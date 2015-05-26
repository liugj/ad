<?php

define ("APPLICATION_PATH", dirname(__FILE__));

$application = new Yaf\Application(APPLICATION_PATH . "/config/application.ini");

$response = $application
    ->bootstrap() /* init custom view in bootstrap */
	->run();

?>
