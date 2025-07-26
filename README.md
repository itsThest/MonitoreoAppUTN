# 📡 MonitoreoApp

**MonitoreoApp** es una aplicación Android desarrollada con Java que permite recolectar datos del dispositivo en tiempo real, incluyendo ubicación GPS, estado de batería, red y almacenamiento. También levanta un servidor HTTP local embebido que expone endpoints REST para consultar estos datos.

---

## 🧠 Funcionalidades

- 📍 Recolección automática de coordenadas GPS cada 30 segundos
- 🔌 Lectura del estado de batería y almacenamiento interno
- 🌐 Visualización de la IP de red local del dispositivo
- 📊 Estadísticas de registros y tiempo activo
- ⚙️ Servidor HTTP embebido (NanoHTTPD) accesible en red local
- 🔐 Protección con token de autenticación para consumir los endpoints

---

## 🖼️ Captura de pantalla

<img src="https://user-images.githubusercontent.com/00000000/device-monitor-preview.png" alt="Pantalla principal" width="300" />
<img width="203" height="440" alt="image" src="https://github.com/user-attachments/assets/4fb70ec5-2e74-4bc5-9216-d4ea9f9dbcc4" />

---

## 🔗 Endpoints REST disponibles

| Método | Endpoint                | Descripción                           |
|--------|--------------------------|---------------------------------------|
| GET    | `/api/sensor_data`       | Retorna los datos de ubicación entre dos fechas |
| GET    | `/api/device_status`     | Retorna nivel de batería, red, espacio y más |

✅ Todos los endpoints requieren autenticación por `Bearer Token`.

---

## 📂 Estructura de carpetas

MonitoreoApp/
├── app/
│ ├── java/com/example/monitoreoapputn/
│ │ ├── MainActivity.java
│ │ ├── HTTPServer.java
│ │ └── DatabaseHelper.java
│ ├── res/layout/activity_main.xml
│ └── AndroidManifest.xml
├── build.gradle.kts
└── README.md

yaml
Copiar
Editar

---

## 🚀 Tecnologías usadas

- 🔧 Android SDK + Java
- 🗺️ GPS API (`LocationManager`)
- 📡 Servidor HTTP: [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd)
- 📁 SQLite (local database)
- 🎨 Material Design

---

## ⚠️ Permisos necesarios

Asegúrate de declarar y solicitar los siguientes permisos:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<img width="203" height="440" alt="image" src="https://github.com/user-attachments/assets/fba1e940-c314-4729-b670-b4392cb752e7" />

👨‍💻 Desarrollador
🧑‍💻 Bixmarck Rodríguez
Estudiante de Ingeniería en Software - Universidad Técnica del Norte
GitHub: @itsThest
