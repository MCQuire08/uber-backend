# 🚗 Uber Backend — Spring Boot + WebSockets + PostgreSQL

Backend en tiempo real para una aplicación tipo Uber. Implementa las funcionalidades core de solicitud de viajes, emparejamiento conductor-pasajero y tracking GPS en vivo.

## 📋 Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| **Java** | 17 | Lenguaje principal |
| **Spring Boot** | 3.2.5 | Framework backend |
| **Spring WebSocket** | STOMP/SockJS | Comunicación en tiempo real |
| **Spring Data JPA** | — | ORM / Persistencia |
| **PostgreSQL** | 14+ | Base de datos relacional |
| **Maven** | 3.9+ | Gestión de dependencias |
| **Lombok** | — | Reducción de boilerplate (opcional) |

## 📁 Estructura del Proyecto

```
uber-backend/
├── src/main/java/com/uber/
│   ├── config/                          # Configuraciones
│   │   ├── WebSocketConfig.java         # STOMP/SockJS config
│   │   └── CorsConfig.java             # CORS para desarrollo
│   ├── controller/                      # Controladores
│   │   ├── UserController.java          # REST: gestión de usuarios
│   │   ├── TripController.java          # REST: gestión de viajes
│   │   └── WebSocketController.java     # WebSocket: tiempo real
│   ├── service/                         # Lógica de negocio
│   │   ├── UserService.java             # Usuarios y perfiles mock
│   │   ├── TripService.java             # Ciclo de vida del viaje
│   │   └── LocationService.java         # Tracking GPS
│   ├── repository/                      # Repositorios JPA
│   │   ├── UserRepository.java
│   │   ├── TripRepository.java
│   │   └── LocationUpdateRepository.java
│   ├── model/                           # Entidades JPA
│   │   ├── User.java
│   │   ├── Trip.java
│   │   ├── LocationUpdate.java
│   │   ├── UserRole.java                # Enum: DRIVER / PASSENGER
│   │   └── TripStatus.java             # Enum: REQUESTED / ACCEPTED / ...
│   ├── dto/                             # Data Transfer Objects
│   │   ├── TripRequestDto.java
│   │   ├── TripResponseDto.java
│   │   ├── LocationUpdateDto.java
│   │   └── DriverStatusDto.java
│   └── UberApplication.java            # Clase principal
├── src/main/resources/
│   └── application.properties           # Configuración de la app
├── pom.xml                              # Dependencias Maven
└── README.md                            # Este archivo
```

## 🚀 Cómo Ejecutar el Proyecto

### Prerrequisitos

1. **Java 17+** instalado
2. **Maven 3.9+** instalado
3. **PostgreSQL 14+** ejecutándose

### Configurar PostgreSQL

```bash
# Conectar a PostgreSQL
psql -U postgres

# Crear la base de datos
CREATE DATABASE uber_db;

# Verificar
\l
```

### Ejecutar la Aplicación

```bash
# Desde la raíz del proyecto
cd uber-backend

# Compilar y ejecutar
mvn spring-boot:run

# O compilar el JAR y ejecutar
mvn clean package -DskipTests
java -jar target/uber-backend-1.0.0-SNAPSHOT.jar
```

La aplicación inicia en `http://localhost:8080`

### Perfiles Mock

Al iniciar, se crean automáticamente dos usuarios de prueba:

| Usuario | Rol | Email | Estado |
|---|---|---|---|
| Cliente A | PASSENGER | clientea@uber.com | — |
| Conductor B | DRIVER | conductorb@uber.com | Online ✅ |

## 🌐 Endpoints REST

### Usuarios

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/users` | Listar todos los usuarios |
| `GET` | `/api/users/{id}` | Obtener usuario por ID |
| `PUT` | `/api/users/{id}/status` | Cambiar estado online/offline |
| `GET` | `/api/users/drivers/available` | Listar conductores disponibles |

### Viajes

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/trips` | Crear solicitud de viaje |
| `GET` | `/api/trips/{id}` | Obtener viaje por ID |
| `GET` | `/api/trips/driver/{driverId}` | Viajes de un conductor |
| `GET` | `/api/trips/passenger/{passengerId}` | Viajes de un pasajero |

## 🔌 WebSocket — Flujo de Mensajes

### Conexión

```
Endpoint: ws://localhost:8080/ws-uber
Protocolo: STOMP sobre SockJS
```

### 1. Solicitar Viaje

**Pasajero envía** a `/app/trip.request`:
```json
{
  "passengerId": 1,
  "originLat": 9.9281,
  "originLng": -84.0907,
  "destinationLat": 9.9500,
  "destinationLng": -84.1200
}
```

**Conductores reciben** en `/topic/driver/notifications`:
```json
{
  "tripId": 1,
  "status": "REQUESTED",
  "passengerId": 1,
  "originLat": 9.9281,
  "originLng": -84.0907,
  "destinationLat": 9.9500,
  "destinationLng": -84.1200,
  "requestedAt": "2026-05-27T10:30:00"
}
```

### 2. Aceptar Viaje

**Conductor envía** a `/app/trip.accept/{tripId}`:
```json
2
```
*(payload: ID del conductor)*

**Pasajero recibe** en `/topic/trips/{tripId}`:
```json
{
  "tripId": 1,
  "status": "ACCEPTED",
  "driverId": 2,
  "driverName": "Conductor B",
  "passengerId": 1,
  "originLat": 9.9281,
  "originLng": -84.0907,
  "destinationLat": 9.9500,
  "destinationLng": -84.1200,
  "requestedAt": "2026-05-27T10:30:00",
  "acceptedAt": "2026-05-27T10:30:15"
}
```

### 3. Rechazar Viaje

**Conductor envía** a `/app/trip.reject/{tripId}`:
*(sin payload necesario)*

**Pasajero recibe** en `/topic/trips/{tripId}`:
```json
{
  "tripId": 1,
  "status": "CANCELLED"
}
```

### 4. Actualizar Ubicación (Tracking)

**Conductor envía** a `/app/location.update`:
```json
{
  "tripId": 1,
  "driverId": 2,
  "latitude": 9.9320,
  "longitude": -84.0880
}
```

**Pasajero recibe** en `/topic/trips/{tripId}/location`:
```json
{
  "tripId": 1,
  "driverId": 2,
  "latitude": 9.9320,
  "longitude": -84.0880
}
```

### Suscripciones WebSocket

| Topic | Suscriptor | Descripción |
|---|---|---|
| `/topic/driver/notifications` | Conductores | Nuevas solicitudes de viaje |
| `/topic/trips/{tripId}` | Pasajero + Conductor | Estado del viaje |
| `/topic/trips/{tripId}/location` | Pasajero | Ubicación GPS del conductor |

## 🧪 Ejemplos con cURL

### Crear un viaje (REST)
```bash
curl -X POST http://localhost:8080/api/trips \
  -H "Content-Type: application/json" \
  -d '{
    "passengerId": 1,
    "originLat": 9.9281,
    "originLng": -84.0907,
    "destinationLat": 9.9500,
    "destinationLng": -84.1200
  }'
```

### Obtener usuario
```bash
curl http://localhost:8080/api/users/1
```

### Cambiar estado del conductor
```bash
curl -X PUT http://localhost:8080/api/users/2/status \
  -H "Content-Type: application/json" \
  -d '{"driverId": 2, "isOnline": true}'
```

### Listar conductores disponibles
```bash
curl http://localhost:8080/api/users/drivers/available
```

## 🏗️ Arquitectura y Principios

### Capas de la Aplicación

```
┌─────────────────────────────────┐
│       Controllers (REST/WS)     │  ← Capa de presentación
├─────────────────────────────────┤
│           Services              │  ← Lógica de negocio
├─────────────────────────────────┤
│         Repositories            │  ← Acceso a datos
├─────────────────────────────────┤
│     Models / Entities           │  ← Dominio
├─────────────────────────────────┤
│         PostgreSQL              │  ← Persistencia
└─────────────────────────────────┘
```

### Principios SOLID Aplicados

- **SRP**: Cada clase tiene una única responsabilidad (UserService gestiona usuarios, TripService gestiona viajes)
- **OCP**: Servicios extensibles mediante interfaces de Spring Data
- **LSP**: Interfaces de repositorio siguen contratos estándar de JPA
- **ISP**: Interfaces específicas por entidad (UserRepository, TripRepository)
- **DIP**: Inyección de dependencias vía constructor en todos los servicios y controladores

## 📝 Configuración de PostgreSQL

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/uber_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Las tablas se crean automáticamente gracias a `spring.jpa.hibernate.ddl-auto=update`.

## 🔮 Extensiones Futuras

- [ ] Autenticación JWT (Spring Security)
- [ ] Cálculo de tarifas basado en distancia
- [ ] Sistema de calificaciones
- [ ] Historial de viajes
- [ ] Notificaciones push
- [ ] Caché con Redis para ubicaciones en tiempo real
- [ ] Búsqueda de conductores por proximidad geográfica (PostGIS)

## 📄 Licencia

Este proyecto es un prototipo educativo / de demostración.
