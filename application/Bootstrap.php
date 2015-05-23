<?php
   /* bootstrap class should be defined under ./application/Bootstrap.php */
class Bootstrap extends Yaf\Bootstrap_Abstract {
	public function _initConfig(Yaf\Dispatcher $dispatcher) {
                Yaf\Registry::set('config', $dispatcher->getApplication()-> getConfig());
	}
	public function _initPlugin(Yaf\Dispatcher $dispatcher) {
	}

	//public function _initRoute(Yaf\Dispatcher $dispatcher) {
	//}
}
