use std::net::{TcpListener, TcpStream};
use std::io::{Read, Write};
use std::convert::TryInto;

// 데이터 구조 정의
#[repr(C)]
struct Data {
    id: u32,
    value: f64,
    name_len: u32,
    name: Vec<u8>,
}

// 데이터 직렬화
fn serialize(data: &Data) -> Vec<u8> {
    let mut buffer = Vec::new();
    buffer.extend(&data.id.to_le_bytes());
    buffer.extend(&data.value.to_le_bytes());
    buffer.extend(&data.name_len.to_le_bytes());
    buffer.extend(&data.name);
    buffer
}

// 데이터 역직렬화
fn deserialize(buffer: &[u8]) -> Data {
    let id = u32::from_le_bytes(buffer[0..4].try_into().unwrap());
    let value = f64::from_le_bytes(buffer[4..12].try_into().unwrap());
    let name_len = u32::from_le_bytes(buffer[12..16].try_into().unwrap());
    let name = buffer[16..(16 + name_len as usize)].to_vec();
    Data { id, value, name_len, name }
}

// 클라이언트로부터 데이터를 받고 다시 보내는 함수
fn handle_client(mut stream: TcpStream) {
    let mut buffer = [0; 1024]; // 데이터 받을 버퍼
    let size = stream.read(&mut buffer).unwrap();

    let data = deserialize(&buffer[0..size]);
    println!("Received: id={}, value={}, name={:?}", data.id, data.value, String::from_utf8(data.name.clone()).unwrap());

    // 다시 클라이언트로 전송
    let serialized = serialize(&data);
    stream.write(&serialized).unwrap();
}

fn main() {
    let listener = TcpListener::bind("127.0.0.1:7878").unwrap();
    println!("Server listening on port 7878");

    // 클라이언트 접속 기다림
    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                handle_client(stream);
            }
            Err(e) => {
                println!("Failed to accept connection: {}", e);
            }
        }
    }
}
