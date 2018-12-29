<?php

class TestCarrera extends CI_Controller {

    public function index() {
        echo "Hello Carrera\n";
    }

    public function produce() {
        $this->load->library('carrera/Carrera');
        $ret = $this->carrera->send('Test', 'Hello World', Carrera::PARTITION_RAND, 0);
        var_dump($ret);
    }

}