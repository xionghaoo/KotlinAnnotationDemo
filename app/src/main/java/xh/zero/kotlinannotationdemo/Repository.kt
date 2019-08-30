package xh.zero.kotlinannotationdemo

import xh.zero.processor.PostEntry

class Repository {

    @PostEntry("Login")
    fun getLoginName() {
        LoginRequestEntry()
    }


}