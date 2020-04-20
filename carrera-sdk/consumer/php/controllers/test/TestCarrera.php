<?php

/**
 * @property Carrera carrera
 */
class TestCarrera extends CI_Controller {

    public function index() {
        echo "Hello Carrera Consumer\n";
    }

    public function consume() {
        $this->load->library('carrera/Carrera');
        $ret = $this->carrera->pull('cg_1', 'Test');
        if ($ret['code'] > 0) {
            var_dump($ret);
            return;
        }
        $context = $ret['ret']['context'];
        $messages = $ret['ret']['messages'];
        $aSuccessOffsets = [];
        $aFailOffsets = [];
        foreach ($messages as $message) {
            if ($this->callback($message->value)) {
                $aSuccessOffsets[] = $message->offset;
            } else {
                $aFailOffsets[] = $message->offset;
            }
        }
        $ret = $this->carrera->submit($context, $aSuccessOffsets, $aFailOffsets);
        var_dump($ret);
    }

    private function callback($value) {
        echo "msg: ".$value."\r\n";
        return (mt_rand(1,100) > 50);
    }
}