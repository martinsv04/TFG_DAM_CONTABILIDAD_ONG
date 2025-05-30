-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: tfg_ong
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `detalles_factura`
--

DROP TABLE IF EXISTS `detalles_factura`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalles_factura` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_factura` bigint DEFAULT NULL,
  `descripcion` varchar(255) NOT NULL,
  `cantidad` int DEFAULT '1',
  `precio` decimal(38,2) NOT NULL,
  `iva` decimal(38,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `detalles_factura_ibfk_1` (`id_factura`),
  CONSTRAINT `detalles_factura_ibfk_1` FOREIGN KEY (`id_factura`) REFERENCES `facturas` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalles_factura`
--

LOCK TABLES `detalles_factura` WRITE;
/*!40000 ALTER TABLE `detalles_factura` DISABLE KEYS */;
INSERT INTO `detalles_factura` VALUES (1,1,'Donación anónima',1,750.00,0.00),(2,2,'Donación anónima',1,750.00,0.00),(3,3,'Donación anónima',1,50.00,0.00),(4,4,'Compra de material educativo',1,200.00,0.00),(5,5,'Transporte Voluntarios',1,45.00,0.00),(6,6,'Sueldo Coordinador IT',1,1200.00,0.00),(7,7,'Donacion para mis queridisimos animales',1,500.00,0.00),(9,8,'Transporte voluntarios',1,45.00,0.00),(10,9,'Donación mensual',1,150.00,0.00);
/*!40000 ALTER TABLE `detalles_factura` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facturas`
--

DROP TABLE IF EXISTS `facturas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facturas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_ong` bigint NOT NULL,
  `id_usuario` bigint DEFAULT NULL,
  `numero` varchar(255) NOT NULL,
  `fecha` date NOT NULL,
  `total` decimal(38,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero` (`numero`),
  KEY `id_ong` (`id_ong`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `facturas_ibfk_1` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`) ON DELETE CASCADE,
  CONSTRAINT `facturas_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facturas`
--

LOCK TABLES `facturas` WRITE;
/*!40000 ALTER TABLE `facturas` DISABLE KEYS */;
INSERT INTO `facturas` VALUES (1,3,NULL,'F-9AC798EB','2025-05-12',750.00),(2,3,NULL,'F-4DE5C102','2025-05-12',750.00),(3,3,NULL,'F-BA572E2A','2025-05-12',50.00),(4,3,NULL,'G-04C8EF62','2025-05-12',200.00),(5,3,NULL,'G-5F4CC731','2025-05-12',45.00),(6,3,NULL,'G-FA747268','2025-05-16',1200.00),(7,3,NULL,'F-3035CDC2','2025-05-30',500.00),(8,1,NULL,'G-F0AFFA0D','2025-05-30',45.00),(9,1,2,'F-AE800CB5','2025-05-30',150.00);
/*!40000 ALTER TABLE `facturas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gastos`
--

DROP TABLE IF EXISTS `gastos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gastos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_ong` bigint NOT NULL,
  `categoria` enum('PERSONAL','SUMINISTROS','ALQUILER','TRANSPORTE','FORMACIÓN','COMUNICACIÓN','OTROS') NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  `monto` decimal(38,2) DEFAULT NULL,
  `fecha` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_ong` (`id_ong`),
  CONSTRAINT `gastos_ibfk_1` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gastos`
--

LOCK TABLES `gastos` WRITE;
/*!40000 ALTER TABLE `gastos` DISABLE KEYS */;
INSERT INTO `gastos` VALUES (1,3,'PERSONAL','Sueldo coordinador proyecto',1200.00,'2025-05-11'),(2,3,'ALQUILER','Alquiler local ONG abril',600.00,'2025-05-11'),(3,3,'SUMINISTROS','Compra de material educativo',180.00,'2025-05-11'),(4,3,'SUMINISTROS','Compra de material educativo',200.00,'2025-05-12'),(5,3,'TRANSPORTE','Transporte Voluntarios',45.00,'2025-05-12'),(6,3,'PERSONAL','Sueldo Coordinador IT',1200.00,'2025-05-16');
/*!40000 ALTER TABLE `gastos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingreso`
--

DROP TABLE IF EXISTS `ingreso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingreso` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `monto` decimal(38,2) DEFAULT NULL,
  `tipo` varchar(255) DEFAULT NULL,
  `id_ong` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgolop720kwilamh50jphpekkp` (`id_ong`),
  CONSTRAINT `FKgolop720kwilamh50jphpekkp` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingreso`
--

LOCK TABLES `ingreso` WRITE;
/*!40000 ALTER TABLE `ingreso` DISABLE KEYS */;
INSERT INTO `ingreso` VALUES (1,'Donación anónima','2025-05-11',500.00,'donaciones',NULL),(2,'Donación anónima','2025-05-11',500.00,'donaciones',3),(3,'Descripción: Subvención Ayuntamiento 2024','2025-05-11',2000.00,'subvenciones',3),(4,'Descripción: Ingreso evento “Corre por una causa”','2025-05-11',750.00,'eventos',3),(5,'Donación anónima','2025-05-12',750.00,'donaciones',3),(6,'Donación anónima','2025-05-12',750.00,'donaciones',3),(7,'Donación anónima','2025-05-12',50.00,'donaciones',3),(8,'Donacion para mis queridisimos animales','2025-05-30',500.00,'DONACIÓN',3);
/*!40000 ALTER TABLE `ingreso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ongs`
--

DROP TABLE IF EXISTS `ongs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ongs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `descripcion` text,
  `direccion` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fecha_creacion` date NOT NULL,
  `id_admin` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_admin` (`id_admin`),
  CONSTRAINT `ongs_ibfk_1` FOREIGN KEY (`id_admin`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ongs`
--

LOCK TABLES `ongs` WRITE;
/*!40000 ALTER TABLE `ongs` DISABLE KEYS */;
INSERT INTO `ongs` VALUES (1,'ONG Esperanza',NULL,NULL,NULL,NULL,'2025-04-19',2),(2,'ONG Esperanza',NULL,NULL,NULL,NULL,'2025-04-19',3),(3,'Protección de Animales Global ','ONG internacional dedicada al rescate y protección de animales en situaciones de emergencia.','Calle Solidaridad 789','600789123','contacto@proteccionanimal.org','2025-05-04',4);
/*!40000 ALTER TABLE `ongs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reportes`
--

DROP TABLE IF EXISTS `reportes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reportes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_ong` bigint NOT NULL,
  `tipo` enum('balance_general','estado_resultados','flujo_efectivo') NOT NULL,
  `fecha_generacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `contenido` text,
  PRIMARY KEY (`id`),
  KEY `id_ong` (`id_ong`),
  CONSTRAINT `reportes_ibfk_1` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reportes`
--

LOCK TABLES `reportes` WRITE;
/*!40000 ALTER TABLE `reportes` DISABLE KEYS */;
INSERT INTO `reportes` VALUES (1,3,'estado_resultados','2025-05-17 13:25:00','Este es un reporte de ejemplo generado manualmente.'),(2,3,'balance_general','2025-05-17 13:30:27','{activos=4800.0, pasivos=0, fondosNetos=1375.0}'),(3,3,'balance_general','2025-05-17 13:30:27','{activos=4800.0, pasivos=0, fondosNetos=1375.0}'),(4,3,'estado_resultados','2025-05-17 13:30:46','{ingresos={donaciones=2050.0, subvenciones=2000.0, eventos=750.0}, totalGastos=3425.0, totalIngresos=4800.0, resultadoNeto=1375.0, gastos={PERSONAL=2400.0, ALQUILER=600.0, SUMINISTROS=380.0, TRANSPORTE=45.0}}'),(5,3,'estado_resultados','2025-05-17 13:30:46','{ingresos={donaciones=2050.0, subvenciones=2000.0, eventos=750.0}, totalGastos=3425.0, totalIngresos=4800.0, resultadoNeto=1375.0, gastos={PERSONAL=2400.0, ALQUILER=600.0, SUMINISTROS=380.0, TRANSPORTE=45.0}}'),(6,3,'balance_general','2025-05-17 13:50:26','{activos=4800.0, pasivos=0, fondosNetos=1375.0}'),(7,3,'balance_general','2025-05-17 13:50:26','{activos=4800.0, pasivos=0, fondosNetos=1375.0}'),(8,3,'estado_resultados','2025-05-17 14:47:51','{\"ingresos\":{\"donaciones\":2050,\"subvenciones\":2000,\"eventos\":750},\"gastos\":{\"PERSONAL\":2400,\"ALQUILER\":600,\"SUMINISTROS\":380,\"TRANSPORTE\":45},\"totalIngresos\":4800,\"totalGastos\":3425,\"resultadoNeto\":1375}'),(9,3,'balance_general','2025-05-17 14:53:10','{\"activos\":4800,\"pasivos\":0,\"fondosNetos\":1375}'),(10,3,'balance_general','2025-05-29 22:58:06','{activos=4800.0, pasivos=0, fondosNetos=1375.0}'),(11,3,'estado_resultados','2025-05-29 23:04:01','{ingresos={donaciones=2050.0, subvenciones=2000.0, eventos=750.0}, totalGastos=3425.0, totalIngresos=4800.0, resultadoNeto=1375.0, gastos={PERSONAL=2400.0, ALQUILER=600.0, SUMINISTROS=380.0, TRANSPORTE=45.0}}'),(12,3,'balance_general','2025-05-30 17:42:28','{activos=4800.0, pasivos=0, fondosNetos=1375.0}');
/*!40000 ALTER TABLE `reportes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('ADMIN','CONTABLE','VOLUNTARIO','DONANTE') NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `nif_cif` varchar(255) DEFAULT NULL,
  `creado_en` date NOT NULL,
  `id_ong` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `FKn9ymjumrloq9actfc632lkndy` (`id_ong`),
  CONSTRAINT `FKn9ymjumrloq9actfc632lkndy` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (2,'Juan Admin','juan@admin.com','123456','ADMIN','600123123','12345678A','2025-04-19',NULL),(3,'Ana Garcíados','anasda@ejemplo.com','123456','ADMIN','1232456789','123425678A','2025-04-19',NULL),(4,'Admin General','admin@example.com','password123','ADMIN','123456789','X1234567Y','2025-04-28',NULL),(5,'Pepe Rodríguez','pepe@example.com','password123','VOLUNTARIO','600654321','12345678A','2025-05-04',3);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-30 22:29:11
