# Configuracion paso a paso EFT

Esta guia resume la configuracion que debe quedar documentada en el informe de
la EFT. Complementa las capturas de `docs/EVIDENCIAS_EFT.md`.

## 1. Azure AD B2C - Identity as a Service

1. Ingresar al portal de Azure y abrir el recurso Azure AD B2C usado por el proyecto.
2. Registrar la aplicacion cliente que solicita tokens JWT.
3. Configurar el flujo de usuario `B2C_1_DuocDemoAzure_registro_login`.
4. Agregar atributos de usuario requeridos, como ciudad y pais/region.
5. Crear el atributo personalizado de rol.
6. Exponer el atributo como claim del token, por ejemplo `extension_Rol`.
7. Emitir tokens para dos usuarios:
   - `ESTUDIANTE`
   - `INSTRUCTOR`
8. Configurar el backend con:

```text
AZURE_B2C_ISSUER_URI=<issuer del flujo B2C>
APP_SECURITY_ROLES_ENABLED=true
```

9. Obtener un JWT desde el flujo de usuario de Azure AD B2C.
10. Ingresar el token en la pantalla inicial del frontend.
11. Confirmar que el frontend muestra la vista principal autenticada y reutiliza
    el token automaticamente en las llamadas al backend.
12. Validar en Postman:
   - Sin token: `401 Unauthorized`.
   - Estudiante consultando cursos: `200 OK`.
   - Estudiante creando curso: `403 Forbidden`.
   - Instructor creando curso: `201 Created`.

## 2. AWS API Gateway - API Manager

1. Crear una API HTTP en AWS API Gateway.
2. Crear las rutas del backend.
3. Asociar los metodos HTTP correspondientes.
4. Crear integraciones HTTP hacia EC2 usando el backend en puerto `8080`.
5. Configurar autorizador JWT con:
   - issuer de Azure AD B2C.
   - audience/client id de la aplicacion.
6. Asociar el autorizador JWT a las rutas protegidas.
7. Desplegar la API.
8. Validar con Postman usando la URL publica de API Gateway.

Rutas que deben quedar registradas:

| Ruta | Metodo |
| --- | --- |
| `/cursos` | `GET`, `POST` |
| `/cursos/{cursoId}/contenidos` | `GET`, `POST` |
| `/cursos/{cursoId}/examenes` | `GET`, `POST` |
| `/calificaciones` | `GET`, `POST` |
| `/examenes/{examenId}/intentos` | `POST` |
| `/intentos/{intentoId}/finalizar` | `POST` |
| `/inscripciones/{inscripcionId}/intentos` | `GET` |
| `/inscripciones` | `POST` |
| `/inscripciones/{numeroResumen}/resumen` | `GET` |
| `/inscripciones/{numeroResumen}/resumenes-mq/producir` | `POST` |
| `/inscripciones/resumenes-mq/consumir` | `POST` |
| `/s3/uploadResumen` | `POST` |
| `/s3/downloadResumen` | `GET` |
| `/s3/updateResumen` | `PUT` |
| `/s3/deleteResumen` | `DELETE` |

## 3. RabbitMQ - Servicio de colas

1. Definir el servicio `rabbitmq` en `docker-compose.ec2.yml`.
2. Usar la imagen `rabbitmq:3-management`.
3. Publicar el puerto `15672` para administracion web.
4. Usar el puerto `5672` internamente para comunicacion AMQP con el backend.
5. Cargar `rabbitmq/rabbitmq.conf`.
6. Cargar `rabbitmq/definitions.json`.
7. Definir variables del backend:

```text
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_RESUMEN_QUEUE=resumen.inscripcion.queue
RABBITMQ_RESUMEN_EXCHANGE=resumen.inscripcion.exchange
RABBITMQ_RESUMEN_ROUTING_KEY=resumen.inscripcion.key
```

8. Confirmar los elementos RabbitMQ:

```text
Queue: resumen.inscripcion.queue
Exchange: resumen.inscripcion.exchange
Routing key: resumen.inscripcion.key
```

9. Ejecutar productor:

```text
POST /inscripciones/{numeroResumen}/resumenes-mq/producir
```

10. Verificar el mensaje en RabbitMQ Management.
11. Ejecutar consumidor:

```text
POST /inscripciones/resumenes-mq/consumir
```

12. Confirmar en Oracle el registro procesado en `RESUMENES_INSCRIPCION_MQ`.

## 4. EC2, Docker y CI/CD

1. Crear secrets de GitHub para DockerHub, EC2, Oracle, AWS, Azure B2C, S3 y RabbitMQ.
2. Ejecutar el workflow de GitHub Actions.
3. El workflow debe:
   - compilar con Maven;
   - ejecutar pruebas;
   - construir imagen backend;
   - construir imagen frontend;
   - publicar imagenes en DockerHub;
   - copiar `docker-compose.ec2.yml` y configuracion RabbitMQ a EC2;
   - ejecutar `docker compose pull`;
   - ejecutar `docker compose up -d`;
   - mostrar `docker compose ps`.
4. En EC2 validar:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Debe mostrar:

```text
formativa-cloud-native
formativa-frontend
formativa-rabbitmq
```

## 5. Flujo de estudiante realizando examen

1. Crear o seleccionar un curso existente.
2. Crear un examen para el curso.
3. Inscribir al estudiante en el curso.
4. Iniciar intento:

```text
POST /examenes/{examenId}/intentos
```

Body:

```json
{
  "inscripcionId": 142
}
```

5. Finalizar intento:

```text
POST /intentos/{intentoId}/finalizar
```

Body:

```json
{
  "puntajeObtenido": 92,
  "respuestas": [
    {
      "pregunta": "Arquitectura cloud native",
      "respuesta": "API Gateway, BFF, RabbitMQ y S3"
    }
  ],
  "comentario": "Examen realizado por estudiante."
}
```

6. Confirmar que la respuesta queda en estado `FINALIZADO`.
7. Confirmar que se genera `calificacionId`.
8. Consultar trazabilidad:

```text
GET /inscripciones/{inscripcionId}/intentos
```

## 6. Evidencias complementarias que conviene capturar

Estas capturas no estaban disponibles en el set revisado, pero ayudan a cerrar
los puntos mas estrictos de la pauta:

| Evidencia | Ubicacion |
| --- | --- |
| `docker ps` con backend, frontend y RabbitMQ `Up` | Terminal EC2 |
| `docker-compose.ec2.yml` mostrando `app`, `frontend` y `rabbitmq` | GitHub o editor |
| Workflow con pasos Maven, build backend, build frontend, push DockerHub y SSH EC2 | GitHub Actions |
| API Gateway mostrando rutas de contenidos, examenes, calificaciones y RabbitMQ | AWS API Gateway |
| Productor y consumidor RabbitMQ probados con URL de API Gateway | Postman |
| Pantalla de ingreso frontend con JWT emitido por Azure AD B2C | Frontend |
| Inicio y finalizacion de intento de examen desde frontend o Postman | Frontend/Postman |
