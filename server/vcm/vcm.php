<?php
	include( 'db.inc' );
	define( 'QUEUE',31417 );
	set_time_limit( 0 );
	ob_implicit_flush( );
	$clients=array( );
	$address='0.0.0.0';
	$port=5095;

	if( ( $sock=socket_create( AF_INET,SOCK_STREAM,SOL_TCP ) ) === false ) {
		return ;
	}

	if( @socket_bind( $sock,$address,$port ) === false ) {
		return ;
	}

	if( socket_listen( $sock ) === false ) {
		return ;
	}

	if( socket_set_nonblock( $sock ) === false ) {
		return ;
	}

	$reciever=msg_get_queue( QUEUE );
	file_put_contents( "vcm.lock",'1' );
	$id=0;

	while( true ) {
		if( !file_exists( "vcm.lock" ) ) {
			echo "Exit daemon\n";
			posix_kill( $id,SIGTERM );
			pcntl_signal_dispatch( );
      break;
    }

    $msg_type=NULL;
    $msg=NULL;
    $max_msg_size=512;
    while( msg_receive( $reciever,0,$msg_type,$max_msg_size,$msg,false,MSG_IPC_NOWAIT ) ) {
      echo date( "H:i:s" )."   free  $msg\n";
      unset( $clients[$msg] );
  	}

  	pcntl_wait( $status,WNOHANG );
  	if( ( $msgsock=socket_accept( $sock ) ) === false ) {
  		sleep( 1 );
  		continue;
  	}

  	if( @socket_write( $msgsock,"#",1 ) === false ) {
  		socket_close( $msgsock );
  		continue;
  	}

  	if( @socket_recv( $msgsock,$uid,16,MSG_DONTWAIT ) === false ) {
  		socket_close( $msgsock );
  		continue;
  	}

  	$uid = trim( $uid );
  	echo date( "H:i:s" )." + open  $uid\n";

  	if( strlen( $uid ) > 8 ) {
  		$clients[$uid]=$msgsock;
  	} else {
  		socket_close( $msgsock );
  	}

  	if( $id ) {
  		//    echo "Reload $id\n";
  		posix_kill( $id,SIGTERM );
  		pcntl_signal_dispatch( );
  	}

  	$id=pcntl_fork( );

  	if( $id==0 ) {
  		declare( ticks=1 );
      pcntl_signal( SIGTERM, function ( $signo ) {
  			exit( 0 );
  		}
      );

  		file_put_contents( "users",print_r( $clients,true ) );
  		$sender=msg_get_queue( QUEUE );
  		socket_close( $sock );
  		$timeout=10;

  		while( count( $clients ) > 0 ) {
  			foreach( $clients as $uid => $insock ) {
  				@socket_recv( $insock,$buf,1024,MSG_DONTWAIT );
  				// Test for new messages here
			    $imei = str_pad( $uid, 16, '0000000000000000', STR_PAD_LEFT );
  				$new_msg=db_fetch_val( "SELECT 1 cnt FROM queue q WHERE q.imei = '$imei'",'cnt' );
  				if( $new_msg ) {
  					if( @socket_write( $insock,"1",1 ) === false ) {
  						echo date( "H:i:s" )." - close $uid\n";
  						socket_close( $insock );
  						unset( $clients[$uid] );
  						msg_send( $sender,1,$uid,false );
    					break;
    				}
    			} else {
    				if( $timeout>=10 ) {
    					if( @socket_write( $insock,"0",1 ) === false ) {
    						echo date( "H:i:s" )." - close $uid\n";
    						socket_close( $insock );
    						unset( $clients[$uid] );
    						msg_send( $sender,1,$uid,false );
      					break;
      				}
      			}
      		}
      	}

      	sleep( 1 );
      	$timeout++;
      	if( $timeout>=10 ) {
      		$timeout=0;
      	}
      }
      echo "All closed\n";
      exit( 0 );
    }
  }

  socket_close( $sock );
  msg_remove_queue ( $reciever )
?>