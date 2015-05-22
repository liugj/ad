<?php 
class ExchangeController extends Yaf\Controller_Abstract {
	public function  SAction() {//默认Action
		$siteId = intval($this->getRequest()->get('siteId', 0));
		$auId   = intval($this->getRequest()->get('auId', 0));

		$adConfig   =  new Yaf\Config\Ini(APP_PATH. '/config/ad.ini');
		$size       = $adConfig->size->toArray();
		$seq        = $adConfig->seq->toArray();
		$mediaUnionRaw = $adConfig->mediaUnion->toArray();
               // 根据siteId 和auId 找媒体联盟
                $mediaUnion = array();
		foreach ($mediaUnionRaw as $key=>$value){
			$mediaUnion[$key]  = $value;
			$mediaList         =  explode(',', $value);
			for ($i=0; $i<sizeof($mediaList)-1; $i++) {
				$mediaUnion[$mediaList[$i]]  = str_replace($mediaList[$i], $key, $value);
			}
		}
                 //选中媒体顺序
                $currentMediaUnion = array();
                $requestMedia = sprintf('%d_%d', $siteId, $auId);
		if (isset($mediaUnion[$requestMedia])) {
			$currentMediaUnion['sites'][$auId] = $siteId; 
			$tempList    = explode(',', $mediaUnion[$requestMedia]);
			for($i=0; $i<sizeof($tempList)-1; $i++){
				$temp       = explode('_', $tempList[$i]);
				$currentMediaUnion['sites'][$temp[1]] = $temp[0];
			}
			$currentMediaUnion['size'] = explode('_', $size[$tempList[$i]]);
                        $currentMediaUnion['seq']  = $seq;
		}

		$this->getView()->assign("media",  $currentMediaUnion);
		$this->getView()->assign("auId",   $auId);
               header('Content-Type: text/plain');
	}
}
