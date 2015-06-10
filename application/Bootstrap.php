<?php
   /* bootstrap class should be defined under ./application/Bootstrap.php */
class Bootstrap extends Yaf\Bootstrap_Abstract {
	public function _initConfig(Yaf\Dispatcher $dispatcher) {
                Yaf\Registry::set('config', $dispatcher->getApplication()-> getConfig());
	}
	public function _initPlugin(Yaf\Dispatcher $dispatcher) {
	}

	public function _initRoute(Yaf\Dispatcher $dispatcher) {
		//$route = new Yaf\Route_Map(
		//		　　'ads/([a-zA-Z]+)',
		//		　　array(
		//			　　　　'controller' => 'exchange',
                //                                'module' => 'newad',
		//			　　　　'action' => 's'
		//			　　),
		//		);
		//$router->addRoute('ads', $route);
#                $route = new Yaf_Route_Regex(
#   　　'#product/([a-zA-Z-_0-9]+)#',
#   　　array(
#　　　　　　'controller' => 'products',
#　　　　　　'action' => 'view'
#   　　)
#   );
#   $router->addRoute('product', $route);
	}
}
