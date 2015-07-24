<?php 
class ExchangeController extends Yaf\Controller_Abstract {
	public function  SAction() {//默认Action
		$siteId = intval($this->getRequest()->get('siteId', 0));
		$auId   = intval($this->getRequest()->get('auId', 0));

		$adConfig   =  new Yaf\Config\Ini(APP_PATH. '/config/ad.ini');
		$size       = $adConfig->size->toArray();
		$seq        = $adConfig->seq->toArray();
        $filename   = sprintf('%s/config/media/%d/%d', APP_PATH, $auId, $siteId);
        $content    = trim(file_get_contents($filename));
        $currentMediaUnion = array('seq'=>$seq);
        $temp = explode(',', $content);
        $currentMediaUnion['size'] = explode('_', $size[$temp[1]]);
        $currentMediaUnion['sites'] [$auId] = $siteId;
        list($nextSiteId, $nextAuId) = explode('_', $temp[0]);
        $currentMediaUnion['sites'] [$auId] = $siteId;
        $currentMediaUnion['sites'] [$nextAuId] = $nextSiteId;
		$this->getView()->assign("media",  $currentMediaUnion);
		$this->getView()->assign("auId",   $auId);
		if ($auId==0|| $auId==3) {
			header('Content-Type: text/plain');
		}else{
			header('Content-Type: text/html');
		}
	}
}
