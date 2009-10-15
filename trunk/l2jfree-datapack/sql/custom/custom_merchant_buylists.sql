DROP TABLE IF EXISTS `custom_merchant_buylists`;
CREATE TABLE `custom_merchant_buylists` AS
  SELECT * FROM `merchant_buylists` WHERE FALSE;
