use std::{fs, io::{self, Write}};

use common::get_source_data;

mod common;

#[tokio::main]
async fn main() {
    println!("Enter username: ");
    let mut username = String::new();
    io::stdin().read_line(&mut username).unwrap();
    println!("Enter password: ");
    let mut password = String::new();
    io::stdin().read_line(&mut password).unwrap();
    let data = get_source_data(&username.trim(), &password.trim(), ".", "Q3", false).await.unwrap();
    fs::File::create("assignments.json").unwrap().write(data.as_bytes()).unwrap();
}