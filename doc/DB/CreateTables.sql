-- Server version	5.6.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `BOAdmin`
--

DROP TABLE IF EXISTS `BOAdmin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOAdmin` (
  `admin_id` char(32) NOT NULL COMMENT '管理员ID',
  `priviledge_roles` varchar(4000) DEFAULT NULL COMMENT '权限分配(dataA:rw|dataB:rw|...|dataN:rw)',
  `name` varchar(45) DEFAULT NULL COMMENT '姓名',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BOBrand`
--

DROP TABLE IF EXISTS `BOBrand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOBrand` (
  `brand_id` char(32) NOT NULL COMMENT '品牌ID',
  `name` varchar(100) DEFAULT NULL COMMENT '品牌名',
  `name_en` varchar(100) DEFAULT NULL COMMENT '英文名',
  `desc` varchar(2000) DEFAULT NULL COMMENT '品牌描述',
  `logo_pic_url` varchar(2000) DEFAULT NULL COMMENT 'logo图片URL(多个的场合逗号分隔)',
  `banner_pic_url` varchar(2000) DEFAULT NULL COMMENT '广告条图片URL(多个的场合逗号分隔)',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`brand_id`),
  UNIQUE KEY `IDX_UNIQ` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='品牌';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BOShop`
--

DROP TABLE IF EXISTS `BOShop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOShop` (
  `shop_id` char(32) NOT NULL COMMENT '门店ID',
  `brand_id` char(32) DEFAULT NULL COMMENT '品牌ID',
  `province_name` varchar(32) DEFAULT NULL COMMENT '省名',
  `city_name` varchar(32) DEFAULT NULL COMMENT '城市名',
  `area_name` varchar(32) DEFAULT NULL COMMENT '区域名',
  `sub_area_name` varchar(32) DEFAULT NULL COMMENT '商圈名',
  `shop_name` varchar(60) DEFAULT NULL COMMENT '门店名称',
  `shop_address` varchar(200) DEFAULT NULL COMMENT '地址',
  `shop_tel` varchar(50) DEFAULT NULL COMMENT '电话',
  `park_enviro` varchar(100) DEFAULT NULL COMMENT '停车环境',
  `traffic_route` varchar(200) DEFAULT NULL COMMENT '交通路线',
  `shop_pic_url` varchar(2000) DEFAULT NULL COMMENT '门店图片URL(多个逗号分隔)',
  `contact_name` varchar(40) DEFAULT NULL COMMENT '联系人',
  `contact_tel` varchar(40) DEFAULT NULL COMMENT '联系电话',
  `business_day` varchar(100) DEFAULT NULL COMMENT '营业日',
  `business_hour` varchar(40) DEFAULT NULL COMMENT '营业时间',
  `shop_num` varchar(40) DEFAULT NULL COMMENT '门店编号',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='门店';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-27  9:40:08
