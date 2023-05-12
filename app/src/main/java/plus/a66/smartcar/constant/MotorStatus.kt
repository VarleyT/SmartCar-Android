package plus.a66.smartcar.constant

enum class MotorStatus(val cmd: String) {
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right"),
    STOP("stop")
}