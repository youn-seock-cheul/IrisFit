package com.example.myapplication.session

enum class RingDirection(val labelKey: String, val startAngle: Float) {
    UP("check_up", 315f),
    DOWN("check_down", 135f),
    LEFT("check_left_direction", 225f),
    RIGHT("check_right_direction", 45f)
}
