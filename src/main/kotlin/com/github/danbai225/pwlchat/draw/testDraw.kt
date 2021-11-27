package com.github.danbai225.pwlchat.draw

import java.awt.Graphics
import javax.swing.JPanel


class testDraw:JPanel() {
    override fun paint(graphics: Graphics) {
        // 必须先调用父类的paint方法
        super.paint(graphics)
        // 用画笔Graphics，在画板JPanel上画一个小人
        graphics.drawOval(100, 70, 30, 30) // 头部（画圆形）
        graphics.drawRect(105, 100, 20, 30) // 身体（画矩形）
        graphics.drawLine(105, 100, 75, 120) // 左臂（画直线）
        graphics.drawLine(125, 100, 150, 120) // 右臂（画直线）
        graphics.drawLine(105, 130, 75, 150) // 左腿（画直线）
        graphics.drawLine(125, 130, 150, 150) // 右腿（画直线）
    }
}