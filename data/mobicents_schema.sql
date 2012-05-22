SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `mobicentsdemo` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `mobicentsdemo`;

-- -----------------------------------------------------
-- Table `mobicentsdemo`.`phoneCall`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `mobicentsdemo`.`phoneCall` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `dateStart` DATETIME NULL ,
  `dateEnd` DATETIME NULL ,
  `fromAddress` VARCHAR(100) NULL ,
  `toAddress` VARCHAR(100) NULL ,
  `dtmf` VARCHAR(300) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 1000;


-- -----------------------------------------------------
-- Table `mobicentsdemo`.`session`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `mobicentsdemo`.`session` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `dateCreated` DATETIME NULL ,
  `dateValidated` DATETIME NULL ,
  `callId` INT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_session_call` (`callId` ASC) ,
  CONSTRAINT `fk_session_call`
    FOREIGN KEY (`callId` )
    REFERENCES `mobicentsdemo`.`phoneCall` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 1000;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
