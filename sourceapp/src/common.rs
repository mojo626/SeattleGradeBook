use regex::Regex;
use reqwest::blocking::ClientBuilder;
use serde::{Deserialize, Serialize};
use serde_json::Value;
use thiserror::Error;
use url::Url;

#[derive(Deserialize, Serialize, Debug)]
struct Class {
    assignments: Value,
    assignments_parsed: Vec<Assignment>,
    frn: String,
    store_code: String,
    url: String,
    name: String,
}

#[derive(Deserialize, Serialize, Debug)]
struct Assignment {
    _assignmentsections: Vec<AssignmentSection>,
}

#[derive(Deserialize, Serialize, Debug)]
struct AssignmentSection {
    _id: i32,
    _name: String,
    assignmentsectionid: i32,
    duedate: String,
    iscountedinfinalgrade: bool,
    isscorespublish: bool,
    isscoringneeded: bool,
    name: String,
    scoreentrypoints: f32,
    scoretype: String,
    sectionsdcid: i32,
    totalpointvalue: f32,
    weight: f32,
    _assignmentscores: Vec<AssignmentScore>,
}

#[derive(Deserialize, Serialize, Debug)]
struct AssignmentScore {
    _name: String,
    actualscoreentered: Option<String>,
    actualscorekind: Option<String>,
    authoredbyuc: bool,
    isabsent: bool,
    iscollected: bool,
    isexempt: bool,
    isincomplete: bool,
    islate: bool,
    ismissing: bool,
    scoreentrydate: String,
    scorelettergrade: Option<String>,
    scorepercent: Option<f32>,
    scorepoints: Option<f32>,
    studentsdcid: i32,
    whenmodified: String,
}

#[derive(Error, Debug)]
pub enum SourceError {
    #[error("Http Error {0}")]
    HttpError(#[from] reqwest::Error),
    #[error("Regex Error {0}")]
    RegexError(#[from] regex::Error),
    #[error("Url Parse Error {0}")]
    UrlParseError(#[from] url::ParseError),
    #[error("Json Parse Error")]
    JsonParseError(#[from] serde_json::Error),
}

pub fn get_source_data(username: &str, password: &str) -> Result<String, SourceError> {
    let client = ClientBuilder::new().cookie_store(true).build()?;
    let session_res = client.get("https://ps.seattleschools.org").send()?;
    let login_body  = [
        ("dbpw", password), 
        ("serviceName", "PS Parent Portal"),
        ("pcasServerUrl", "/"),
        ("credentialType", "User Id and Password Credential"),
        ("ldappassword", password),
        ("account", username),
        ("pw", password),
    ];
    session_res.error_for_status()?;
    let login_res = client.post("https://ps.seattleschools.org/guardian/home.html").form(&login_body).send()?;
    let home_html = login_res.text()?;

    let scores_html_regex = Regex::new("<a href=\"scores.html\\?frn=[^\\\"]*\"")?;
    let score_url_regex = Regex::new("\"s[^\"]*")?;
    let class_header_regex = Regex::new("<td class=\"table-element-text-align-start\">[^&]*")?;
    let class_headers = class_header_regex.find_iter(&home_html).map(|class_header_match| {
        class_header_match.as_str()["<td class=\"table-element-text-align-start\">".len()..].to_string()
    }).collect::<Vec<_>>();

    let mut class_frns = Vec::new();
    for score_href in scores_html_regex.find_iter(&home_html) {
        let Some(score_url_match) = score_url_regex.find(score_href.as_str()) else {
            println!("couldn't find url from href {}", score_href.as_str());
            continue; 
        };
        let score_url = &score_url_match.as_str()[1..];
        let full_score_url = Url::parse(&format!("https://ps.seattleschools.org/guardian/{}", score_url))?;
        let Some((_, class_frn)) = full_score_url.query_pairs().into_iter().find(|(key, _)| key == "frn") else { continue; };
        let Some((_, store_code)) = full_score_url.query_pairs().into_iter().find(|(key, _)| key == "fg") else { continue; };
        if store_code != "S1" && store_code != "S2" {
            continue;
        }
        class_frns.push((full_score_url.to_string(), class_frn.to_string(), store_code.to_string()));
        
    }
    
    
    let data_ng_regex: Regex = Regex::new("data-ng-init=\"[^\"]*")?;
    let student_frn_regex: Regex = Regex::new("studentFRN = '[^']*")?;
    let section_id_regex: Regex = Regex::new("data-sectionid=\"[^\"]*")?;

    let mut assignments: Vec<Class> = Vec::new();

    for (i, (full_score_url, class_frn, store_code)) in class_frns.into_iter().enumerate() {
        let scores_html_res = client.get(&full_score_url).send()?;
        let scores_text = scores_html_res.text()?;
        let Some(data_ng) = data_ng_regex.find(&scores_text) else {
            println!("no data_ng found at {full_score_url}");
            continue;
        };
        println!("{}", data_ng.as_str());

        let Some(section_id_match) = section_id_regex.find(&scores_text) else { continue; };
        let section_id = &section_id_match.as_str()["data-sectionid=\"".len()..];
        let Some(student_frn_match) = student_frn_regex.find(data_ng.as_str()) else { continue; };
        let student_frn = &student_frn_match.as_str()[student_frn_match.as_str().len()-6..];
        println!("section_id: {section_id}, student_frn: {student_frn}, store_code: {store_code}");
        let assignments_res = client.post(format!("https://ps.seattleschools.org/ws/xte/assignment/lookup"))
            .body(format!("{{\"section_ids\":[{section_id}],\"student_ids\":[{student_frn}], \"store_codes\": [\"{store_code}\"]}}"))
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Referer", &full_score_url)
            .header("Accept", "application/json, text/plain, */*")
            .send()?;
        let assignments_json = assignments_res.text()?;
        println!("{assignments_json}");
        assignments.push(Class {
            frn: class_frn.into(),
            assignments: serde_json::from_str(&assignments_json)?, 
            assignments_parsed: serde_json::from_str(&assignments_json)?, 
            url: full_score_url, 
            store_code, 
            name: class_headers[i].clone(), 
        });
    }
    println!("{assignments:?}");
    Ok(serde_json::to_string_pretty(&assignments)?)
}