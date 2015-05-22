<?php 
define("APP_PATH",  realpath(dirname(__FILE__)) . '/../'); 
$app  = new Yaf\Application(APP_PATH . "/config/application.ini");
$app->bootstrap()->run();
