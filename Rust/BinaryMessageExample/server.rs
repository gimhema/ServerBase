use std::io::{self, Cursor, Read, Write};

// 데이터 패킹을 위한 구조체 정의
#[repr(packed)]
#[derive(Debug, Clone)]
struct PackedData {
    id: u32,       // 4 bytes
    size: u32,     // 4 bytes
    value: u64,    // 8 bytes
}

impl PackedData {
    fn new(id: u32, value: u64) -> Self {
        let size = std::mem::size_of::<PackedData>() as u32; // 구조체의 크기
        PackedData { id, size, value }
    }

    // 바이너리 직렬화
    fn serialize(&self) -> Vec<u8> {
        let mut buffer = Vec::with_capacity(self.size as usize);
        let mut cursor = Cursor::new(&mut buffer);
        cursor.write_all(&self.id.to_le_bytes()).unwrap();
        cursor.write_all(&self.size.to_le_bytes()).unwrap();
        cursor.write_all(&self.value.to_le_bytes()).unwrap();
        buffer
    }

    // 바이너리 역직렬화
    fn deserialize(buffer: &[u8]) -> io::Result<Self> {
        let mut cursor = Cursor::new(buffer);
        let mut id_bytes = [0u8; 4];
        let mut size_bytes = [0u8; 4];
        let mut value_bytes = [0u8; 8];

        cursor.read_exact(&mut id_bytes)?;
        cursor.read_exact(&mut size_bytes)?;
        cursor.read_exact(&mut value_bytes)?;

        let id = u32::from_le_bytes(id_bytes);
        let size = u32::from_le_bytes(size_bytes);
        let value = u64::from_le_bytes(value_bytes);

        if size != buffer.len() as u32 {
            return Err(io::Error::new(io::ErrorKind::InvalidData, "Size mismatch"));
        }

        Ok(PackedData { id, size, value })
    }
}

fn main() {
    // 새로운 PackedData 인스턴스 생성
    let data = PackedData::new(1, 123456789);
    
    // 바이너리 직렬화
    let serialized = data.serialize();
    println!("Serialized data: {:?}", serialized);

    // 바이너리 역직렬화
    match PackedData::deserialize(&serialized) {
        Ok(deserialized_data) => {
            println!("Deserialized data: {:?}", deserialized_data);
        }
        Err(e) => {
            println!("Error during deserialization: {}", e);
        }
    }
}
