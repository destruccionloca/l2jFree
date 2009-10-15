DROP TABLE IF EXISTS `custom_merchant_shopids`;
CREATE TABLE `custom_merchant_shopids` AS
  SELECT * FROM `merchant_shopids` WHERE FALSE;
