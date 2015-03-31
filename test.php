<?php
$fp = fsockopen("192.168.0.2", 23, $errno, $errstr, 3);

if (!$fp) {
  die("$errstr ($errno)\n");
} 

$b = array();
$x = 10;

for ($i = $x; $i > 0; $i--) {
  $t = microtime(TRUE);
  $a = 0;
  while (microtime(TRUE) - $t < 1.0) {

    fwrite($fp, "Hello\n"); //writes a new line of data
    echo fgets($fp); //gets the newest available line

    $a++;
  }
  $b[] = $a;
}

$c = array_sum($b)/$x;
echo "After {$x} trials, device performed an average of {$c} queries per second.";
fclose($fp);
?>
