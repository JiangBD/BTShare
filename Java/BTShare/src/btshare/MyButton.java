/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package btshare;

import javafx.scene.control.Button;

public class MyButton extends Button {

    private BTDevice device;
    
    public MyButton() {
        super();
    }
    public BTDevice getBTDevice() { return device; }
    public void updateDisplay() {
        this.setText(device.getHfName() + "\n" + device.getBtAddr());
    
    }
    
    public MyButton(BTDevice d){
    super();
    this.device = d;
    }
    
}
