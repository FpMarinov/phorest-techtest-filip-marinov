# Backend Technical Test
> A REST API that allows the management of clients and their appointments, purchases and services.

## Table of Contents
* [General Info](#general-information)
* [Technologies Used](#technologies-used)
* [Setup](#setup)
* [Usage](#usage)
* [API Documentation](#api-documentation)
* [Contact](#contact)


## General Information
**Comb as You Are** have decided to ditch their old salon software provider
and upgrade to *Phorest* to avail of its best in class client retention tools.
They're excited to finally offer their clients the opportunity to book online.
They've exported their clients appointment data from their old provider and
would like to email their top 50 most loyal clients of the past year with the news
that they can now book their next appointment online.

****Problem Spec****

The exported data is split across 4 files.

- **clients.csv**
- **appointments.csv**
- **services.csv**
- **purchases.csv**

Each client has many appointments and are related through a `client_id` property on the appointment.
Each appointment has many services and are related through an `appointment_id` property on the service.
Each appointments has 0 or many purchases and are related through an `appointment_id` property on the purchase.
Services and purchases have an associated number of loyalty points defined as a property.
Clients have a boolean banned property defined on the client.

## Technologies Used
- Java - version 17
- Spring Boot - version 3.1.2
- springdoc openapi - version 2.2.0
- spotless maven plugin - version 2.38.0
- opencsv - version 5.8
- modelmapper - version 3.1.1
- flyway - version 9.21.1
- testcontainers - bom version 1.18.3
- PostgreSQL - version 15.4
- Docker
- Docker Compose
- Maven


## Setup
The project requires the Java SDK version 17.
The additional dependencies are listed in the pom.xml file in the working directory
and are managed by Maven. Docker Compose can also be set up
for local running of the project.


## Usage
The project can be started by:

1. Using the "mvn package" command from a terminal window in the working directory.
(the tests require Docker to be set up since they use test containers)
2. Using the "docker-compose up" command from a terminal window
in the working directory.

The default server url is http://localhost:5071. The API can be tested through:
- The Swagger UI at http://localhost:5071/swagger-ui.html.
- Postman.


## API Documentation
```yaml
openapi: 3.0.1
info:
title: Backend Technical Test
description: Client API
contact:
	name: Filip Marinov
	email: fp.marinov@gmail.com
version: 1.0.0
servers:
- url: http://localhost:5071
	description: Generated server url
paths:
/services/{serviceId}:
	get:
	tags:
		- Service Operations
	summary: Get Service
	operationId: getService
	parameters:
		- name: serviceId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/ServiceResponse'
	put:
	tags:
		- Service Operations
	summary: Update Service
	operationId: updateService
	parameters:
		- name: serviceId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	requestBody:
		content:
		application/json:
			schema:
			$ref: '#/components/schemas/ServiceRequest'
		required: true
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/ServiceResponse'
	delete:
	tags:
		- Service Operations
	summary: Delete Service
	operationId: deleteService
	parameters:
		- name: serviceId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"204":
		description: No Content
/purchases/{purchaseId}:
	get:
	tags:
		- Purchase Operations
	summary: Get Purchase
	operationId: getPurchase
	parameters:
		- name: purchaseId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/PurchaseResponse'
	put:
	tags:
		- Purchase Operations
	summary: Update Purchase
	operationId: updatePurchase
	parameters:
		- name: purchaseId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	requestBody:
		content:
		application/json:
			schema:
			$ref: '#/components/schemas/PurchaseRequest'
		required: true
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/PurchaseResponse'
	delete:
	tags:
		- Purchase Operations
	summary: Delete Purchase
	operationId: deletePurchase
	parameters:
		- name: purchaseId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"204":
		description: No Content
/clients/{clientId}:
	get:
	tags:
		- Client Operations
	summary: Get Client
	operationId: getClient
	parameters:
		- name: clientId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/ClientResponse'
	put:
	tags:
		- Client Operations
	summary: Update Client
	operationId: updateClient
	parameters:
		- name: clientId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	requestBody:
		content:
		application/json:
			schema:
			$ref: '#/components/schemas/ClientRequest'
		required: true
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/ClientResponse'
	delete:
	tags:
		- Client Operations
	summary: Delete Client
	operationId: deleteClient
	parameters:
		- name: clientId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"204":
		description: No Content
/appointments/{appointmentId}:
	get:
	tags:
		- Appointment Operations
	summary: Get Appointment
	operationId: getAppointment
	parameters:
		- name: appointmentId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/AppointmentResponse'
	put:
	tags:
		- Appointment Operations
	summary: Update Appointment
	operationId: updateAppointment
	parameters:
		- name: appointmentId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	requestBody:
		content:
		application/json:
			schema:
			$ref: '#/components/schemas/AppointmentRequest'
		required: true
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				$ref: '#/components/schemas/AppointmentResponse'
	delete:
	tags:
		- Appointment Operations
	summary: Delete Appointment
	operationId: deleteAppointment
	parameters:
		- name: appointmentId
		in: path
		required: true
		schema:
			type: string
			format: uuid
	responses:
		"204":
		description: No Content
/services/files:
	post:
	tags:
		- Service Operations
	summary: Create new Services by uploading a csv file
	operationId: createServicesFromFile
	requestBody:
		content:
		multipart/form-data:
			schema:
			required:
				- file
			type: object
			properties:
				file:
				type: string
				format: binary
	responses:
		"201":
		description: Created
/purchases/files:
	post:
	tags:
		- Purchase Operations
	summary: Create new Purchases by uploading a csv file
	operationId: createPurchasesFromFile
	requestBody:
		content:
		multipart/form-data:
			schema:
			required:
				- file
			type: object
			properties:
				file:
				type: string
				format: binary
	responses:
		"201":
		description: Created
/clients/files:
	post:
	tags:
		- Client Operations
	summary: Create new Clients by uploading a csv file
	operationId: createClientsFromFile
	requestBody:
		content:
		multipart/form-data:
			schema:
			required:
				- file
			type: object
			properties:
				file:
				type: string
				format: binary
	responses:
		"201":
		description: Created
/appointments/files:
	post:
	tags:
		- Appointment Operations
	summary: Create new Appointments by uploading a csv file
	operationId: createAppointmentsFromFile
	requestBody:
		content:
		multipart/form-data:
			schema:
			required:
				- file
			type: object
			properties:
				file:
				type: string
				format: binary
	responses:
		"201":
		description: Created
/clients/top:
	get:
	tags:
		- Client Operations
	summary: Get top Clients
	operationId: getTopClients
	parameters:
		- name: number
		in: query
		required: true
		schema:
			type: integer
			format: int32
		- name: cutoff
		in: query
		required: true
		schema:
			type: string
			format: date
	responses:
		"200":
		description: OK
		content:
			application/json:
			schema:
				type: array
				items:
				$ref: '#/components/schemas/ClientResponse'
components:
schemas:
	ServiceRequest:
	required:
		- loyalty_points
		- name
		- price
	type: object
	properties:
		name:
		maxLength: 50
		minLength: 0
		type: string
		price:
		type: number
		loyalty_points:
		type: integer
		format: int32
	ServiceResponse:
	type: object
	properties:
		id:
		type: string
		format: uuid
		name:
		type: string
		price:
		type: number
		loyalty_points:
		type: integer
		format: int32
	PurchaseRequest:
	required:
		- loyalty_points
		- name
		- price
	type: object
	properties:
		name:
		maxLength: 50
		minLength: 0
		type: string
		price:
		type: number
		loyalty_points:
		type: integer
		format: int32
	PurchaseResponse:
	type: object
	properties:
		id:
		type: string
		format: uuid
		name:
		type: string
		price:
		type: number
		loyalty_points:
		type: integer
		format: int32
	ClientRequest:
	required:
		- banned
		- email
		- first_name
		- gender
		- last_name
		- phone
	type: object
	properties:
		first_name:
		maxLength: 50
		minLength: 0
		type: string
		last_name:
		maxLength: 50
		minLength: 0
		type: string
		email:
		type: string
		phone:
		maxLength: 15
		minLength: 0
		type: string
		gender:
		type: string
		enum:
			- MALE
			- FEMALE
		banned:
		type: boolean
	ClientResponse:
	type: object
	properties:
		id:
		type: string
		format: uuid
		first_name:
		type: string
		last_name:
		type: string
		email:
		type: string
		phone:
		type: string
		gender:
		type: string
		enum:
			- MALE
			- FEMALE
		banned:
		type: boolean
	AppointmentRequest:
	required:
		- end_time
		- start_time
	type: object
	properties:
		start_time:
		type: string
		format: date-time
		end_time:
		type: string
		format: date-time
	AppointmentResponse:
	type: object
	properties:
		id:
		type: string
		format: uuid
		start_time:
		type: string
		format: date-time
		end_time:
		type: string
		format: date-time

```


## Contact
Created by [Filip Marinov](https://github.com/FpMarinov).
