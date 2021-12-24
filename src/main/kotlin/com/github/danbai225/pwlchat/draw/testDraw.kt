package com.github.danbai225.pwlchat.draw

import java.awt.Graphics
import javax.swing.JPanel


class testDraw : JPanel() {
    override fun paint(graphics: Graphics) {
        // 必须先调用父类的paint方法
        super.paint(graphics)

        graphics.drawLine(100, 50, 135, 120)
        graphics.drawLine(100, 50, 65, 120)

        graphics.drawLine(135, 120, 125, 115)
        graphics.drawLine(65, 120, 75, 115)

        graphics.drawLine(125, 115, 160, 165)
        graphics.drawLine(75, 115, 40, 165)

        graphics.drawLine(160, 165, 130, 150)
        graphics.drawLine(40, 165, 70, 150)

        graphics.drawLine(130, 150, 170, 185)
        graphics.drawLine(70, 150, 30, 185)

        graphics.drawLine(170, 185, 95, 190)
        graphics.drawLine(30, 185, 95, 190)

        graphics.drawRect(95, 190, 10, 50)

    }
}