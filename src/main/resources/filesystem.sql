/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 50723
 Source Host           : localhost:3306
 Source Schema         : filesystem

 Target Server Type    : MySQL
 Target Server Version : 50723
 File Encoding         : 65001

 Date: 02/06/2022 14:34:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_fs
-- ----------------------------
DROP TABLE IF EXISTS `test_fs`;
CREATE TABLE `test_fs` (
  `id` varchar(255) DEFAULT NULL,
  `author_name` varchar(255) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `major_name` varchar(255) DEFAULT NULL,
  `file_type` varchar(255) DEFAULT NULL,
  `file_size` varchar(255) DEFAULT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `time` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of test_fs
-- ----------------------------
BEGIN;
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('a333e3cbccac4f498ee9357416749a58', '张三', '李享.pdf', 'menu item 2', 'application/pdf', '787230', '/Users/Shared/fileSystem/李享.pdf', '2022-06-01-12:14:11');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('1531851591206916097', NULL, '新建文件夹1', NULL, NULL, NULL, '/Users/Shared/fileSystem//新建文件夹1', '2022-06-01-12:14:14');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('1531851613700968449', NULL, '新建文件夹1-1', NULL, NULL, NULL, '/Users/Shared/fileSystem/新建文件夹1/新建文件夹1-1', '2022-06-01-12:14:19');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('dcaa1232acdf413c879fb07b3cf08f40', '张三', '算法基础入门班第四课.pdf', 'menu item 2', 'application/pdf', '217314', '/Users/Shared/fileSystem/新建文件夹1/算法基础入门班第四课.pdf', '2022-06-01-12:14:49');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('282c2270d63944a0b177e43257d86843', '李四', 'skynet_apdu.pl', 'menu item 2', 'text/x-perl-script', '190105', '/Users/Shared/fileSystem/新建文件夹1/新建文件夹1-1/skynet_apdu.pl', '2022-06-01-12:15:03');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('1531851841942409218', NULL, '新建文件夹1-2', NULL, NULL, NULL, '/Users/Shared/fileSystem/新建文件夹1/新建文件夹1-2', '2022-06-01-12:15:14');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('2848d48e8eb146edabbf97e0df13bd38', '张三', '算法中级班第三课.pdf', 'menu item 2', 'application/pdf', '220823', '/Users/Shared/fileSystem/新建文件夹1/新建文件夹1-2/算法中级班第三课.pdf', '2022-06-01-01:16:44');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('1531867443612393473', NULL, '新建文件夹2', NULL, NULL, NULL, '/Users/Shared/fileSystem//新建文件夹2', '2022-06-01-01:17:13');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('38ac3c6177b64c119f285ae1961b02c5', '李四', '20220506-182253.png.jpeg', 'menu item 2', 'image/jpeg', '8164', '/Users/Shared/fileSystem/新建文件夹2/20220506-182253.png.jpeg', '2022-06-01-01:17:39');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('5b664e8311a54dee9bcf40ab3d463e90', '1232', 'bugreport-mayfly-SKQ1.220303.001-2009-01-01-08-00-58.txt', 'menu item 3', 'text/plain', '35814568', '/Users/Shared/fileSystem/bugreport-mayfly-SKQ1.220303.001-2009-01-01-08-00-58.txt', '2022-06-01-03:22:45');
INSERT INTO `test_fs` (`id`, `author_name`, `file_name`, `major_name`, `file_type`, `file_size`, `file_path`, `time`) VALUES ('1531902266242564097', NULL, '新建文件夹3', NULL, 'dir', NULL, '/Users/Shared/fileSystem//新建文件夹3', '2022-06-01-03:35:36');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
