<?php 
$s  ='st1=300_250
st2=960_90
st3=728_90
st4=200_200
st5=250_250
st6=650_90
st7=960_80
st8=650_80
st9=300_100
st10=1000_90
st11=1000_60
st12=120_270';
$mz  =[];
$content =  file_get_contents('media.txt');
$items = f2c($content, $s, $mz);
 mkdir_media($items); 
$mz_script =  gen_shell_script($items); 
$items =[];
foreach ($mz  as $key=> &$value){
    $value .='@'. $mz_script[$key];
    $items[] = str_replace('@', ',', $value);
}
file_put_contents('media.csv', implode("\n", $items));
function gen_shell_script($items){
        $ad = [];
	$shells = [];
	$patterns = [];
	$patterns[0] = 'wget http://192.168.100.3/newad/exchange/s?siteId=%d -O %d.js';
	$patterns[1] = "wget 'http://192.168.100.3/newad/exchange/s?auId=1&siteId=%d' -O %s.html";
	foreach ($items as $key=> $value) {
		list($id, $channel)  =explode("_", $key) ;
		if ($channel ==0) {
		  	$shells[]= sprintf($patterns[0],  $id, $id);
                        $ad [$id] = sprintf('http://www.adhufeng.com/newad/exchange/s?siteId=%d', $id);
		}else{
			$shells [] = sprintf($patterns[1],  $id, $key);
		}
	}
        file_put_contents('shell.sh', implode("\n", $shells));
        return $ad;
}
function mkdir_media($items)  {
	foreach ($items as $key=> $value) {
           list($id, $channel)  =explode("_", $key) ;
            $dir = sprintf('media/%d',  $channel);
            $file = sprintf('%s/%d', $dir, $id);
            if (!file_exists($dir)) mkdir($dir, 0755, true);
            file_put_contents($file, $value);
	}
}
function  f2c($content, $s, &$mz) {
	$results = [];
	$map = array();
	$lines = explode("\n", $s);
	foreach ($lines as $line) {
		list($key, $value) = explode('=', $line);
		$map[$key] = "#".$value."#";
	}
	$lines = explode("\n", $content);
	foreach ($lines as $line ) {
		if (!$line) continue;

		$items = explode('@', $line);
		$items[5] = str_replace('*', '_', $items[5]);
		$items[5] = preg_replace(array_values($map), array_keys($map), $items[5]);
		$results[$items[9]."_0"] = sprintf('%d_1,%s', $items[8], $items[5]);
		$results[$items[8]."_1"] = sprintf('%d_0,%s', $items[9], $items[5]);
		$mz [$items[9]] = $line;
	}
	return $results;

}
