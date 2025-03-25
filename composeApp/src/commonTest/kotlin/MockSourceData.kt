class MockSourceData {
    companion object {
        val exampleNoAssignments = """
        {
            "student_name": "",
            "grade_level": "11",
            "classes": [
              {
                "assignments_parsed": [],
                "frn": "",
                "store_code": "Q3",
                "url": "",
                "name": "AP ENG LANG COMP 2",
                "teacher_name": "",
                "teacher_contact": "",
                "reported_grade": "B",
                "reported_score": "100"
              },
            ],
            "past_classes": [],
        }
        """

        val exampleThreeAssignments = """
        {
            "student_name": "",
            "grade_level": "11",
            "classes": [
              {
                "assignments_parsed": [
                    {
                        "_assignmentsections": [
                          {
                            "_assignmentcategoryassociations": [
                              {
                                "_id": 2226821,
                                "_name": "assignmentcategoryassoc",
                                "_teachercategory": {
                                  "_name": "teachercategory",
                                  "color": "4",
                                  "description": "This Category is for High and Middle Schools only who use Point calculations",
                                  "name": "Test"
                                },
                                "assignmentcategoryassocid": 2226821
                              }
                            ],
                            "_assignmentscores": [
                              {
                                "_name": "assignmentscore",
                                "actualscoreentered": "14",
                                "actualscorekind": "REAL_SCORE",
                                "authoredbyuc": false,
                                "isabsent": false,
                                "iscollected": false,
                                "isexempt": false,
                                "isincomplete": false,
                                "islate": false,
                                "ismissing": false,
                                "scoreentrydate": "2025-02-09 16:19:29",
                                "scorelettergrade": "C+",
                                "scorepercent": 77.777778,
                                "scorepoints": 14.0,
                                "studentsdcid": 162876,
                                "whenmodified": "2025-02-09"
                              }
                            ],
                            "_id": 2227314,
                            "_name": "assignmentsection",
                            "assignmentsectionid": 2227314,
                            "duedate": "2025-01-30",
                            "iscountedinfinalgrade": false,
                            "isscorespublish": true,
                            "isscoringneeded": true,
                            "name": "Unit 8 FRQ original",
                            "scoreentrypoints": 18.0,
                            "scoretype": "POINTS",
                            "sectionsdcid": 681185,
                            "totalpointvalue": 18.0,
                            "weight": 1.0
                          }
                        ],
                        "_id": 978786,
                        "_name": "assignment",
                        "assignmentid": 978786,
                        "hasstandards": false,
                        "standardscoringmethod": "GradeScale"
                      },
                      {
                        "_assignmentsections": [
                          {
                            "_assignmentcategoryassociations": [
                              {
                                "_id": 2226823,
                                "_name": "assignmentcategoryassoc",
                                "_teachercategory": {
                                  "_name": "teachercategory",
                                  "color": "4",
                                  "description": "This Category is for High and Middle Schools only who use Point calculations",
                                  "name": "Test"
                                },
                                "assignmentcategoryassocid": 2226823
                              }
                            ],
                            "_assignmentscores": [
                              {
                                "_name": "assignmentscore",
                                "actualscoreentered": "4",
                                "actualscorekind": "REAL_SCORE",
                                "authoredbyuc": false,
                                "isabsent": false,
                                "iscollected": false,
                                "isexempt": false,
                                "isincomplete": false,
                                "islate": false,
                                "ismissing": false,
                                "scoreentrydate": "2025-02-09 16:19:29",
                                "scorelettergrade": "E",
                                "scorepercent": 50.0,
                                "scorepoints": 8.0,
                                "studentsdcid": 162876,
                                "whenmodified": "2025-03-10"
                              }
                            ],
                            "_id": 2227316,
                            "_name": "assignmentsection",
                            "assignmentsectionid": 2227316,
                            "duedate": "2025-01-29",
                            "iscountedinfinalgrade": false,
                            "isscorespublish": true,
                            "isscoringneeded": true,
                            "name": "Unit 8 MCQ Original",
                            "scoreentrypoints": 16.0,
                            "scoretype": "POINTS",
                            "sectionsdcid": 681185,
                            "totalpointvalue": 16.0,
                            "weight": 1.0
                          }
                        ],
                        "_id": 978787,
                        "_name": "assignment",
                        "assignmentid": 978787,
                        "hasstandards": false,
                        "standardscoringmethod": "GradeScale"
                      },
                      {
                        "_assignmentsections": [
                          {
                            "_assignmentcategoryassociations": [
                              {
                                "_id": 2227352,
                                "_name": "assignmentcategoryassoc",
                                "_teachercategory": {
                                  "_name": "teachercategory",
                                  "color": "2",
                                  "name": "Homework"
                                },
                                "assignmentcategoryassocid": 2227352
                              }
                            ],
                            "_assignmentscores": [
                              {
                                "_name": "assignmentscore",
                                "actualscoreentered": "2",
                                "actualscorekind": "REAL_SCORE",
                                "authoredbyuc": false,
                                "isabsent": false,
                                "iscollected": false,
                                "isexempt": false,
                                "isincomplete": false,
                                "islate": false,
                                "ismissing": false,
                                "scoreentrydate": "2025-02-10 08:42:26",
                                "scorelettergrade": "A",
                                "scorepercent": 100.0,
                                "scorepoints": 2.0,
                                "studentsdcid": 162876,
                                "whenmodified": "2025-02-10"
                              }
                            ],
                            "_id": 2227845,
                            "_name": "assignmentsection",
                            "assignmentsectionid": 2227845,
                            "duedate": "2025-01-30",
                            "iscountedinfinalgrade": true,
                            "isscorespublish": true,
                            "isscoringneeded": true,
                            "name": "HW 7.1/7.2",
                            "scoreentrypoints": 2.0,
                            "scoretype": "POINTS",
                            "sectionsdcid": 681185,
                            "totalpointvalue": 2.0,
                            "weight": 1.0
                          }
                        ],
                        "_id": 979054,
                        "_name": "assignment",
                        "assignmentid": 979054,
                        "hasstandards": false,
                        "standardscoringmethod": "GradeScale"
                      },
                ],
                "frn": "00464166547",
                "store_code": "Q3",
                "url": "",
                "name": "AP ENG LANG COMP 2",
                "teacher_name": "",
                "teacher_contact": "",
                "reported_grade": "A",
                "reported_score": "100"
              },
            ],
            "past_classes": [],
        }
        """
    }
}