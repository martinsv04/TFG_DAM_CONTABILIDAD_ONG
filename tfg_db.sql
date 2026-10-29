-- ONGestión · estructura de la base de datos MySQL `tfg_ong` (solo tablas, sin datos).
-- Uso:  mysql -u root -p < tfg_db.sql
-- El backend usa spring.jpa.hibernate.ddl-auto=update, así que también puede crear/actualizar las
-- tablas por sí solo al arrancar; este archivo sirve para montar la base de golpe y documentarla.
-- Tablas: ongs, usuarios, ingreso, gastos, facturas, detalles_factura, proyectos, subvenciones,
--         reportes, auditoria, mensajes_contacto.
CREATE DATABASE  IF NOT EXISTS `tfg_ong` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `tfg_ong`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Database: tfg_ong
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
-- Table structure for table `auditoria`
--

DROP TABLE IF EXISTS `auditoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accion` varchar(10) NOT NULL,
  `descripcion` varchar(600) DEFAULT NULL,
  `entidad` varchar(20) NOT NULL,
  `entidad_id` bigint DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  `id_ong` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `usuario_nombre` varchar(150) DEFAULT NULL,
  `usuario_rol` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_auditoria_ong_fecha` (`id_ong`,`fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `id_gasto` bigint DEFAULT NULL,
  `id_ingreso` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_factura_ong_numero` (`id_ong`,`numero`),
  KEY `id_usuario` (`id_usuario`),
  KEY `FK6kwb915i84bcswnbjo1y83flx` (`id_gasto`),
  KEY `FKqhj9rp8eekp7ewv3mdv2pc39i` (`id_ingreso`),
  CONSTRAINT `facturas_ibfk_1` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`) ON DELETE CASCADE,
  CONSTRAINT `facturas_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK6kwb915i84bcswnbjo1y83flx` FOREIGN KEY (`id_gasto`) REFERENCES `gastos` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FKqhj9rp8eekp7ewv3mdv2pc39i` FOREIGN KEY (`id_ingreso`) REFERENCES `ingreso` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `id_proyecto` bigint DEFAULT NULL,
  `cuenta` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_ong` (`id_ong`),
  KEY `FKc5uc0m7uytmpequodf6rdow6p` (`id_proyecto`),
  CONSTRAINT `FKc5uc0m7uytmpequodf6rdow6p` FOREIGN KEY (`id_proyecto`) REFERENCES `proyectos` (`id`) ON DELETE SET NULL,
  CONSTRAINT `gastos_ibfk_1` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `id_usuario` bigint DEFAULT NULL,
  `id_proyecto` bigint DEFAULT NULL,
  `id_subvencion` bigint DEFAULT NULL,
  `cuenta` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgolop720kwilamh50jphpekkp` (`id_ong`),
  KEY `FK523ha1122j3b48jdksuq9lwfx` (`id_usuario`),
  KEY `FKt8q3octm084ngij07dxhp42x2` (`id_proyecto`),
  KEY `FK1uhsy2fkols1muj4ullmgn42j` (`id_subvencion`),
  CONSTRAINT `FK1uhsy2fkols1muj4ullmgn42j` FOREIGN KEY (`id_subvencion`) REFERENCES `subvenciones` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK523ha1122j3b48jdksuq9lwfx` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKgolop720kwilamh50jphpekkp` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`),
  CONSTRAINT `FKt8q3octm084ngij07dxhp42x2` FOREIGN KEY (`id_proyecto`) REFERENCES `proyectos` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mensajes_contacto`
--

DROP TABLE IF EXISTS `mensajes_contacto`;
CREATE TABLE `mensajes_contacto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `organizacion` varchar(150) DEFAULT NULL,
  `email` varchar(254) NOT NULL,
  `mensaje` varchar(2000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  `nif` varchar(255) DEFAULT NULL,
  `representante` varchar(255) DEFAULT NULL,
  `firma` longtext,
  `logo` longtext,
  PRIMARY KEY (`id`),
  KEY `id_admin` (`id_admin`),
  CONSTRAINT `ongs_ibfk_1` FOREIGN KEY (`id_admin`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proyectos`
--

DROP TABLE IF EXISTS `proyectos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proyectos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(1000) DEFAULT NULL,
  `estado` varchar(20) NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `fecha_inicio` date DEFAULT NULL,
  `nombre` varchar(150) NOT NULL,
  `presupuesto` decimal(38,2) DEFAULT NULL,
  `id_ong` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8q7yc6flgr00ysa7qpnqk4nad` (`id_ong`),
  CONSTRAINT `FK8q7yc6flgr00ysa7qpnqk4nad` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subvenciones`
--

DROP TABLE IF EXISTS `subvenciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subvenciones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` varchar(20) NOT NULL,
  `expediente` varchar(60) DEFAULT NULL,
  `fecha_concesion` date DEFAULT NULL,
  `fecha_justificacion` date DEFAULT NULL,
  `fecha_solicitud` date DEFAULT NULL,
  `financiador` varchar(150) DEFAULT NULL,
  `importe_concedido` decimal(38,2) DEFAULT NULL,
  `importe_solicitado` decimal(38,2) DEFAULT NULL,
  `nombre` varchar(150) NOT NULL,
  `id_ong` bigint NOT NULL,
  `id_proyecto` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdtshpvrcmkotpf3p68prggo2v` (`id_ong`),
  KEY `FKkmxygdwxay4oc3yvoym68h5xh` (`id_proyecto`),
  CONSTRAINT `FKdtshpvrcmkotpf3p68prggo2v` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`),
  CONSTRAINT `FKkmxygdwxay4oc3yvoym68h5xh` FOREIGN KEY (`id_proyecto`) REFERENCES `proyectos` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `direccion` varchar(255) DEFAULT NULL,
  `codigo_postal` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `FKn9ymjumrloq9actfc632lkndy` (`id_ong`),
  CONSTRAINT `FKn9ymjumrloq9actfc632lkndy` FOREIGN KEY (`id_ong`) REFERENCES `ongs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'tfg_ong'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

