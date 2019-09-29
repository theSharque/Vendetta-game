<?php
$lang = substr($_SERVER['HTTP_ACCEPT_LANGUAGE'], 0, 2);
switch ($lang){
    case "ru":
        include("index_ru.php");
        break;
    case "en":
        include("index_en.php");
        break;        
    default:
        include("index_en.php");
        break;
}
?>