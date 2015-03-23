/* 
 * Copyright 2015 Mark Longstaff-Tyrrell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frisnit.pacman;

/**
 *
 * @author mark
 */
public class Status {
    
    private boolean interruptEnabled=false;
    private boolean soundEnabled=false;
    private boolean screenFlipped=false;
    private boolean playerOneStartLamp=false;
    private boolean playerTwoStartLamp=false;

    private int irqVector=0;

    /**
     * @return the interruptEnabled
     */
    public boolean isInterruptEnabled() {
        return interruptEnabled;
    }

    /**
     * @param interruptEnabled the interruptEnabled to set
     */
    public void setInterruptEnabled(boolean interruptEnabled) {
        
      //  System.out.println(String.format("setInterruptEnabled %s",interruptEnabled?"enabled":"disabled"));
        
        this.interruptEnabled = interruptEnabled;
    }

    /**
     * @return the irqVector
     */
    public int getIntVector() {
        return irqVector;
    }

    /**
     * @param irqVector the irqVector to set
     */
    public void setIrqVector(int irqVector) {
        this.irqVector = irqVector;
    }

    /**
     * @return the soundEnabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * @param soundEnabled the soundEnabled to set
     */
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    /**
     * @return the screenFlipped
     */
    public boolean isScreenFlipped() {
        return screenFlipped;
    }

    /**
     * @param screenFlipped the screenFlipped to set
     */
    public void setScreenFlipped(boolean screenFlipped) {
        this.screenFlipped = screenFlipped;
    }

    /**
     * @return the playerOneStartLamp
     */
    public boolean isPlayerOneStartLamp() {
        return playerOneStartLamp;
    }

    /**
     * @param playerOneStartLamp the playerOneStartLamp to set
     */
    public void setPlayerOneStartLamp(boolean playerOneStartLamp) {
        this.playerOneStartLamp = playerOneStartLamp;
    }

    /**
     * @return the playerTwoStartLamp
     */
    public boolean isPlayerTwoStartLamp() {
        return playerTwoStartLamp;
    }

    /**
     * @param playerTwoStartLamp the playerTwoStartLamp to set
     */
    public void setPlayerTwoStartLamp(boolean playerTwoStartLamp) {
        this.playerTwoStartLamp = playerTwoStartLamp;
    }
    
}
