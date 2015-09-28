<?php 
class ExchangeController extends Yaf\Controller_Abstract {
    public function  SAction() {//默认Action
        $siteId = intval($this->getRequest()->get('siteId', 0));
        $auId   = intval($this->getRequest()->get('auId', 0));
        $ver    = $this->getRequest()->get('ver', '');     
        $id    = $this->getRequest()->get('id', 0);     

        $adConfig   =  new Yaf\Config\Ini(APP_PATH. '/config/ad.ini');
        $size       = $adConfig->size->toArray();
        $seq        = $adConfig->seq->toArray();
        $currentMediaUnion = array('seq'=>$seq,'sites'=>array());
        $filename   = sprintf('%s/config/media/%d/%d', APP_PATH, $auId, $siteId);
        if (file_exists($filename)) {
            $content    = trim(file_get_contents($filename));
            $temp = explode(',', $content);
            $currentMediaUnion['size'] = explode('_', $size[$temp[1]]);
            list($nextSiteId, $nextAuId) = explode('_', $temp[0]);
            if (isset($temp[2])) {
                list($siteId, $auId) = explode('_', $temp[2]);
            }
            $currentMediaUnion['sites'] [$auId] = $siteId;
            if (isset($temp[3])) {
                list($midSiteId, $midAuId) = explode('_', $temp[3]);
                $currentMediaUnion['sites'] [$midAuId] = $midSiteId;
                $ver = 'r99';
            }
            $currentMediaUnion['sites'] [$nextAuId] = $nextSiteId;
        }

        $this->getView()->assign("media",  $currentMediaUnion);
        $this->getView()->assign("auId",   $auId);
        $this->getView()->assign("ver",   $ver);
        $this->getView()->assign("id",   $id);
        if ($auId == 0|| $auId == 3) {
            header('Content-Type: text/plain'); 
        }else{
            header('Content-Type: text/html');
        }
    }
}
