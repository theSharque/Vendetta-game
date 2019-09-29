<?php

  include( 'db.inc' );
  global $user;

  $img = file_get_contents( 'php://input' );

  if( auth() && strlen( $img ) ) {

    $ref = isset( $_GET['r'] ) ? $_GET['r'] : 0;
    db_safe( $ref );

    $fid = md5( $user['imei'].date( 'dmYHis' ) );
    $fname = "photos/$fid.jpg";

//    $img = base64_decode( $_POST['data'] );
    file_put_contents( $fname, $img );

    list( $width, $height, $type ) = getimagesize( $fname );
    $size = min( $width, $height );
    $photo = imagecreatefromjpeg( $fname );
    $cropped = imagecreatetruecolor( 320, 320 );
    imagecopyresampled( $cropped, $photo, 0, 0, 0, 0, 320, 320, $size, $size );
    imagejpeg( $cropped, $fname, 100 );

    db_query( "DELETE FROM photos WHERE user_id = {$user['id']} AND status IN( 0, 1, 2 )" );
    db_query( "INSERT INTO photos ( file, user_id, status ) VALUES ( '$fid', {$user['id']}, 2 )" );
//    db_query( "INSERT INTO photos ( file, user_id ) VALUES ( '$fid', {$user['id']} )" );
    $id = mysql_insert_id();

    db_query( "UPDATE users SET photo_id = $id WHERE id = {$user['id']}" );

    if( $ref != 0 && is_numeric( $ref ) ) {
      $game_id = db_fetch_val( "SELECT u.game_id
                                  FROM users u
                            INNER JOIN games g ON g.id = u.game_id
                                 WHERE u.id = $ref AND g.status = 1", 'game_id' );

      if( $game_id > 0 ) {
      	db_query( "UPDATE users u SET game_id = $game_id WHERE id = {$user['id']}" );
        $game = db_fetch_row( "SELECT COUNT( u.id ) in_game, g.start_count, g.admin_id
                                 FROM games g
                           INNER JOIN users u ON u.game_id = g.id
                                WHERE g.id = $game_id" );
        
       	if( $game['in_game'] == $game['start_count'] ) {
          sendMessage( $game['admin_id'], 0, 10, $game_id, "" );
        }
      }
    }

    printOut( "err=0&eof" );
  } else {
    printOut( "err=1&eof" );
  }
