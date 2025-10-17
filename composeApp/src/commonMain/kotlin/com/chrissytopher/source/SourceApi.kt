package com.chrissytopher.source

import coil3.toUri
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.parseUrlEncodedParameters
import io.ktor.util.flattenEntries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext

class SourceApi(private val httpClient: HttpClient, private val json: Json, private val downloadDir: Path) {
//    fun getSourceDataFlow(username: String, password: String, quarter: String, loadPfpSynchronously: Boolean, previous: SourceData?): Flow<Result<SourceData>> = flow {
//        val previous =
//        emit(getSourceData(username, password, quarter, ))
//    }

    suspend fun getSourceData(username: String, password: String, quarter: String, loadPfp: Boolean, loadPfpSynchronously: Boolean, previous: SourceData?): Result<SourceData> = runCatching {
        val initRes = httpClient.get("https://ps.seattleschools.org/public/home.html").errorForResponse()
        val loginRes = httpClient.post("https://ps.seattleschools.org/guardian/home.html") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(Parameters.build {
                append("dbpw", password)
                append("serviceName", "PS Parent Portal")
                append("pcasServerUrl", "/")
                append("credentialType", "User Id and Password Credential")
                append("ldappassword", password)
                append("account", username)
                append("pw", password)
            }))
        }.errorForResponse()
        val homeHtml = httpClient.get("https://ps.seattleschools.org/guardian/home.html").bodyAsText()
        val homeDom = Ksoup.parse(homeHtml)

        val imageTask = if (loadPfp) CoroutineScope(coroutineContext).async {
            downloadImage()
        } else null

        val scoresHtmlRegex = Regex("<a href=\"scores.html\\?frn=[^\"]*\"")
        val scoresAHtmlRegex = Regex("<a href=\"scores.html\\?frn=[^\"]*\".+?(?=</a>)</a>")
        val scoreABodyRegex = Regex(">.+?(?=</a>)")
        val nameRegex = Regex("<span id=\"firstlast\">[^<]*")

        val studentName = nameRegex.find(homeHtml)?.value?.substring("<span id=\"firstlast\">".length) ?: throw Exception("Login failed")
        val scoreUrlRegex = Regex("\"s[^\"]*")

        val teachers = previous?.teachers ?: getTeachers(homeHtml).getOrThrow()

        val tabs = homeDom.getElementsByClass("tabs").mapNotNull { it.childElementsList().map { it.childElementsList() }.flatten().map { it.childElementsList() }.flatten().filter { println(it); it.tagName() == "span" && it.attr("for").startsWith("Grades ") } }.flatten()
        val tabsUrls = tabs.map { "https://ps.seattleschools.org/guardian/home.html?schoolid=${it.attr("for").substring("Grades ".length)}" }
        val tabsHtml = if (tabsUrls.isEmpty()) {
            listOf(homeHtml)
        } else {
            val tabsHtmlAsync = tabsUrls.map { CoroutineScope(coroutineContext).async { httpClient.get(it) } }
            tabsHtmlAsync.awaitAll().map { it.bodyAsText() }
        }
        println(tabsUrls)
        val classFrns = mutableListOf<ClassInfo>()
        for (scoreA in tabsHtml.map { scoresAHtmlRegex.findAll(it).toList() }.flatten() ) {
            val scoreHref = scoresHtmlRegex.find(scoreA.value)?.value
            if (scoreHref == null) {
                println("couldn't find inner score from a $scoreA")
                continue
            }
            val scoreUrlMatch = scoreUrlRegex.find(scoreHref)?.value
            if (scoreUrlMatch == null) {
                print("couldn't find url from href $scoreHref")
                continue
            }
            val scoreUrl = scoreUrlMatch.substring(1)
            val fullScoreUrl = "https://ps.seattleschools.org/guardian/$scoreUrl".toUri()
            val classFrn = fullScoreUrl.query?.parseUrlEncodedParameters()?.flattenEntries()?.find { (key, _) -> key == "frn" }?.second ?: continue
            val storeCode = fullScoreUrl.query?.parseUrlEncodedParameters()?.flattenEntries()?.find { (key, _) -> key == "fg" }?.second ?: continue
            if (storeCode != quarter) {
                continue
            }
            val reportedGrade = scoreABodyRegex.find(scoreA.value)?.let { scoreABody ->
                var reportedGrade = scoreABody.value.substring(1)
                var reportedScore = ""
                reportedGrade.indexOf("<br>").let { if (it == -1) null else it }?.let { br_i ->
                    reportedScore = reportedGrade.substring(br_i+4)
                    reportedGrade = reportedGrade.substring(0, br_i)
                }
                Pair(reportedGrade, reportedScore)
            }
            classFrns.add(ClassInfo(fullScoreUrl.toString(), classFrn, storeCode, reportedGrade))
        }

        //for running start
        //class = "tabs"

        val classes = classFrns.map {
            CoroutineScope(coroutineContext).async {
                getClass(it.fullScoreUrl, it.classFrn, it.storeCode, it.reportedGrade?.first, it.reportedGrade?.second, quarter, teachers)
            }
        }.awaitAll().mapNotNull { it.getOrThrow() }

        val gradeLevelRegex = Regex("<tr><td class=\"lbl\">Grade Level:</td><td>[^<]*")
        val gradeLevel = gradeLevelRegex.find(homeHtml)?.value?.substring("<tr><td class=\"lbl\">Grade Level:<\\/td><td".length)

        val pastClasses = previous?.past_classes ?: getPastGrades().getOrThrow()

        if (loadPfpSynchronously) {
            imageTask?.await()
        }

        return@runCatching SourceData(
            classes,
            gradeLevel,
            studentName,
            pastClasses,
            teachers,
        )
    }

    suspend fun getTeachers(homeHtml: String): Result<HashMap<String, String>> = runCatching {
        val teachers = hashMapOf<String, String>()
        val teacherUrlRegex = Regex("<a href=\"teacherinfo.html\\?frn=[^\"]*\"")
        for (teacherUrlMatch in teacherUrlRegex.findAll(homeHtml)) {
            val teacherUrl = teacherUrlMatch.value.substring("<a href=\"".length..teacherUrlMatch.value.length-1)
            val fullUrl = "https://ps.seattleschools.org/guardian/$teacherUrl"
            val teacherHtml = httpClient.get(fullUrl).bodyAsText()
            val teacherNameRegex = Regex("<p><strong>Name:</strong>[^<]*")
            val teacherNameMatch = teacherNameRegex.find(teacherHtml)
            if (teacherNameMatch == null) {
                teachers.put("", "")
                continue
            }
            val teacherName = teacherNameMatch.value.substring("<p><strong>Name:</strong>".length).trim()
            val teacherContactRegex = Regex("<a href=\"[^\"]*")
            val teacherContactMatch = teacherContactRegex.find(teacherHtml)
            if (teacherContactMatch == null) {
                teachers.put(teacherName, "")
                continue
            }
            val teacherContact = teacherContactMatch.value.substring("<a href=\"".length)
            teachers.put(teacherName, teacherContact)
        }
        teachers
    }

    suspend fun getClass(fullScoreUrl: String, classFrn: String, storeCode: String, reportedGrade: String?, reportedScore: String?, quarter: String, teachers: HashMap<String, String>): Result<Class?> = runCatching {
        val dataNgRegex = Regex("data-ng-init=\"[^\"]*")
        val studentFrnRegex = Regex("studentFRN = '[^']*")
        val sectionIdRegex = Regex("data-sectionid=\"[^\"]*")

        val scoresText = httpClient.get(fullScoreUrl).bodyAsText()
        val dataNg = dataNgRegex.find(scoresText)?.value
        if (dataNg == null) {
            println("no data_ng found at {full_score_url}")
            return@runCatching null
        };

        val sectionIdMatch = sectionIdRegex.find(scoresText) ?: return@runCatching null
        val sectionId = sectionIdMatch.value.substring("data-sectionid=\"".length)
        val studentFrnMatch = studentFrnRegex.find(dataNg) ?: return@runCatching null
        val studentFrn = studentFrnMatch.value.substring(studentFrnMatch.value.length-6)
        val assignmentsJson = httpClient.post("https://ps.seattleschools.org/ws/xte/assignment/lookup") {
            setBody("{\"section_ids\":[$sectionId],\"student_ids\":[$studentFrn], \"store_codes\": [\"$quarter\"]}")
            header("Content-Type", "application/json;charset=UTF-8")
            header("Referer", fullScoreUrl)
            header("Accept", "application/json, text/plain, */*")
        }.bodyAsText()

        val (className, teacherName) = getClassNameAndTeacher(scoresText).getOrThrow()
        val nameSplit = teacherName.split(" ")
        var firstName = nameSplit.getOrNull(1) ?: ""
        if (firstName.endsWith(",")) {
            firstName = firstName.substring(0, firstName.length-1)
        }
        var lastName = nameSplit.getOrNull(0) ?: ""
        if (lastName.endsWith(",")) {
            lastName = lastName.substring(0, lastName.length-1)
        }
        var teacherFullName = "$firstName $lastName"

        val parsed = runCatching { json.decodeFromString<List<Assignment>>(assignmentsJson) }.also {
            it.exceptionOrNull()?.let {
                println("problem with $assignmentsJson")
            }
        }.getOrThrow()

        Class(
            frn = classFrn,
//            assignments = serde_json::from_str(&assignments_json)?,
            assignments_parsed = parsed,
            url = fullScoreUrl,
            store_code = storeCode,
            name = className,
            teacher_contact = teachers[teacherFullName] ?: "",
            teacher_name = teacherFullName,
            reported_grade = reportedGrade.let { if (it == "[ i ]") "-" else it },
            reported_score = reportedScore,
        )
    }

    suspend fun downloadImage() {
        val photoHtml = httpClient.get("https://ps.seattleschools.org/guardian/student_photo.html").bodyAsText()
        val photoDom: Document = Ksoup.parse(html = photoHtml)
        photoDom.getElementsByClass("user-photo").firstOrNull()?.let { photoContainer ->
            photoContainer.children().firstOrNull()?.let { imageElement ->
                imageElement.attribute("src")?.value?.let { src ->
                    val pfpBytes = httpClient.get("https://ps.seattleschools.org$src").bodyAsBytes()
                    SystemFileSystem.createDirectories(downloadDir)
                    SystemFileSystem.sink(Path(downloadDir, "pfp.jpeg")).buffered().use {
                        it.write(pfpBytes)
                    }
                }
            }
        }
    }

    suspend fun getPastGrades(): Result<List<PastClass>> = runCatching {
        val gradesHtml = httpClient.get("https://ps.seattleschools.org/guardian/termgrades.html").bodyAsText()
        val gradesDom = Ksoup.parse(html = gradesHtml)
        gradesDom.getElementsByTag("tbody").firstOrNull()?.also { table_body ->
            val classes = mutableListOf<PastClass>()
            for (tableRow in table_body.children()) {
                if (tableRow.tagName() == "tr") {
                    val tableData = tableRow.children().map { it.html() }
                    if (tableData.size == 8) {
                        tableData[5].trim().toFloatOrNull()?.also { creditEarned ->
                            tableData[6].trim().toFloatOrNull()?.also { creditAttempted ->
                                classes.add(PastClass(
                                    date_completed = tableData[0],
                                    grade_level = tableData[1],
                                    school = tableData[2],
                                    course_id = tableData[3],
                                    course_name = tableData[4],
                                    credit_earned = creditEarned,
                                    credit_attempted = creditAttempted,
                                    grade = tableData[7],
                                ))
                            } ?: println("attempted is ${tableData[6].trim()}")
                        } ?: println("earned is ${tableData[5].trim()}")
                    } else {
                        println("table data is not 8 it is ${tableData.size}")
                    }
                } else {
                    println("found child but not tr ${tableRow.tagName()}")
                }
            }
            return@runCatching classes
        } ?: println("no tbody")
        return@runCatching emptyList<PastClass>()
    }

    fun getClassNameAndTeacher(classHtml: String): Result<Pair<String, String>> = runCatching {
        val gradesDom = Ksoup.parse(classHtml)
        gradesDom.getElementsByTag("table").firstOrNull { it.id() == "" && it.className() == "linkDescList" }?.also { table ->
            table.children().firstOrNull()?.also { tableBody ->
                tableBody.children().apply { removeFirstOrNull() }.removeFirstOrNull()?.also { tableRow ->
                    if (tableRow.tagName() == "tr") {
                        val tableData = tableRow.children().map { it.html() }
                        if (tableData.size == 6) {
                            val className = tableData[0]
                            val classTeacher = tableData[1]
                            return@runCatching Pair(className, classTeacher)
                        } else {
                            println("table data is not 6 it is $tableData")
                        }
                    } else {
                        println("found child but not tr ${tableRow.tagName()}")
                    }
                } ?: println("didn't find the table row")
            } ?: println("no table body")
        } ?: println("no table")
        return@runCatching Pair("Unknown Class", "Unknown Teacher")
    }
}

fun HttpResponse.errorForResponse(): HttpResponse = apply {
    if (status.value in 400 until 600) {
        throw Exception("Http response was $status")
    }
}

data class ClassInfo(
    val fullScoreUrl: String,
    val classFrn: String,
    val storeCode: String,
    val reportedGrade: Pair<String, String>?,
)