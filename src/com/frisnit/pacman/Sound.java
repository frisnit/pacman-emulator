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

import dsp.FirFilter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author mark
 */

/*
Register
￼￼￼￼￼
Voice 1 Waveform
Address
5045h
Notes
￼￼low 3 bits used – selects waveform 0-7 from ROM
￼
￼￼
￼
5050h-5054h 20 bits in low nibbles
5055h ￼ ￼ ￼ ￼ low nibble – 0 off to 15 loudest
5040h-5044h low nibbles, used by H/W only
504Ah low 3 bits used – selects waveform 0-7 from ROM
5056h-5059h ￼ ￼ ￼ ￼ 16 bits in low nibbles
505Ah low nibble – 0 off to 15 loudest
5046h-5049h ￼ ￼ ￼ ￼ low nibbles, used by H/W only
504Fh ￼ ￼ ￼ ￼ low 3 bits used – selects waveform 0-7 from ROM
505Bh-505Eh 16 bits in low nibbles
505Fh ￼ ￼ ￼ ￼ low nibble – 0 off to 15 loudest
504Bh-504Eh low nibbles, used by H/W only
*/


public class Sound {

    
    Io io;
    Rom rom;
    
    Status status;
    
    VoiceParameters voiceParameters;
    
    SourceDataLine line;

    FirFilter firFilter;
    
    private static final int FRAMES_PER_SECOND = 60;
    private static final int SOURCE_SAMPLE_RATE = 96000;
    private static final int OUTPUT_SAMPLE_RATE = 22050;
    
    public Sound(Io io, Status status, String rompack)
    {           
        this.io=io;
        this.status=status;
        
        // load sound ROMs
        HashMap<String, Integer> romList = new HashMap<String, Integer>();
        romList.put("82s126.1m", 0x0000);
        romList.put("82s126.3m", 0x0100);
        rom = new Rom(0x200,0x100, romList, rompack);

        // create low pass filter for downsampler
        firFilter = new FirFilter();
        firFilter.setFilter(128,0,(double)OUTPUT_SAMPLE_RATE/(double)SOURCE_SAMPLE_RATE);
        
        voiceParameters = new VoiceParameters();
        
        openSound();
    }
    
    private class VoiceParameters
    {
        private static final int VOICE1 = 0;
        private static final int VOICE2 = 1;
        private static final int VOICE3 = 2;
        
        // 16 32 sample waveforms in the sound ROMs
        private static final int WAVEFORMS = 16;
        private static final int SAMPLES_PER_WAVEFORMS = 32;
        
        int[] frequency;
        int[] volume;
        int[] waveform;
        int[] accumulator;
        
        // sound samples preloaded from sound ROM
        int[][] samples;
        
        public VoiceParameters()
        {
            frequency = new int[3];
            volume = new int[3];
            waveform = new int[3];
            accumulator = new int[3];
            samples = new int[WAVEFORMS][];
            
            // preload waveforms from ROMs
            for(int i=0;i<WAVEFORMS;i++)
            {
                samples[i]=getSample(i);
            }            
        }            
        
        public void UpdateVoiceParameters()
        {
            UpdateVoiceParameters(VOICE1);
            UpdateVoiceParameters(VOICE2);
            UpdateVoiceParameters(VOICE3);
        }
        
        public void UpdateVoiceParameters(int voice)
        {
            // get voice parameters out of the IO component
            switch(voice)
            {
                case VOICE1:
                    waveform[0]  = getWaveformIndex(0x5045);
                    frequency[0] = getFrequency(0x5050,5);
                    volume[0]    = getVolume(0x5055);
                    
                //    if(frequency[0]>0)
                 //       System.out.println(String.format("Voice 1 volume 0x%05x",volume[0]));
                    
                    break;
                case VOICE2:
                    waveform[1]  = getWaveformIndex(0x504a);
                    frequency[1] = getFrequency(0x5056,4)<<4;
                    volume[1]    = getVolume(0x505a);
                 //   if(frequency[1]>0)
                 //       System.out.println(String.format("Voice 2 volume 0x%05x",volume[1]));

                    break;
                case VOICE3:
                    waveform[2]  = getWaveformIndex(0x504f);
                    frequency[2] = getFrequency(0x505b,4)<<4;
                    volume[2]    = getVolume(0x505f);
                    
                   // if(frequency[2]>0)
                     //   System.out.println(String.format("Voice 3 volume 0x%05x",volume[2]));
                    
                  //  System.out.println(String.format("volumes: %d, %d, %d", volume[0],volume[1],volume[2]));
                    
                    
                    break;
                default:
                    break;
            }
        }
        
        // return a single sample, adjusted for frequency and volume
        private int getNextSample(int voice)
        {
            // get selected sample waveform
            int[] wave = samples[waveform[voice]];

            // get next sample byte
            // index into wave is is top 5 bits in accumulator
            int offset = (accumulator[voice]>>15)&0x1f;
            int sample = wave[offset];
            
            // update accumulator
            accumulator[voice]+=frequency[voice];

            // looking at the schematic, the 4-bit volume parameter is applied to the enable lines of 4 analogue switches
            // (the input of each switch is from a weighted resistor network) so it's an & rather than a multiply
            int output = sample & volume[voice];
            
            // maximise volume of 4-bit samples for output
            return output<<=3;
        }
        
        private int getWaveformIndex(int address)
        {
            return io.readSoundData(address)&0x07;
        }
        
        // get low nibbles from frequency bytes
        // store them as a single 20-bit value

        // 4444 3333 2222 1111 0000
        
        private int getFrequency(int address, int length)
        {
            int frequency=0;

            for(int i=length-1;i>=0;i--)            
            {
            
                frequency<<=4;
                
// two voices have 4-nibble frequencies
               // if(i<=length)
                    frequency|=(io.readSoundData(address+i)&0x0f);
             
            }
            
            frequency&=0xfffff;
            
            
            
            return frequency;
        }
        
        private int getVolume(int address)
        {
            return (io.readSoundData(address)&0x0f);//>>1;
        }

        /*
            4-bit samples, so each byte entry has the top nibble set to 0.
            This gives in total 512 four-bit sound samples.
            These are organized into 16 waveforms, each 32 samples long, and each sample value from 0-15.
        */
        // get waveform from sound ROM
        private int[] getSample(int sampleNumber)
        {
            int[] sample = new int[SAMPLES_PER_WAVEFORMS];
            int offset = sampleNumber*SAMPLES_PER_WAVEFORMS;
                        
            for(int i=0;i<WAVEFORMS;i++)
            {
                sample[i]=rom.readByte(i+offset);
            }

            return sample;
        }
    
        // return PCM data of currently playing sound on voice 'voice'
        public byte[] generateVoiceWaveform(int voice, int frameSize)
        {
            byte[] frame = new byte[frameSize];

            for(int i=0;i<frameSize;i++)
            {
                frame[i]=(byte)voiceParameters.getNextSample(voice);
            }

            return frame;
        }

        /*
        public byte[] generateSoundFrame(int frameSize)
        {
            voiceParameters.UpdateVoiceParameters();

            byte[] voice1 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE1,frameSize/3);
            byte[] voice2 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE2,frameSize/3);
            byte[] voice3 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE3,frameSize/3);

            byte[] output = new byte[frameSize];

            for(int i=0;i<frameSize/3;i++)
            {
                output[i*3+0]=voice1[i];
                output[i*3+1]=voice2[i];
                output[i*3+2]=voice3[i];
            }
            return output;
        }      
        */
        
                
        public byte[] generateSoundFrame(int frameSize)
        {
            voiceParameters.UpdateVoiceParameters();

            byte[] voice1 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE1,frameSize);
            byte[] voice2 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE2,frameSize);
            byte[] voice3 = voiceParameters.generateVoiceWaveform(VoiceParameters.VOICE3,frameSize);

            byte[] output = new byte[frameSize];

            // mix the three channels together
            for(int i=0;i<frameSize;i++)
            {
                int mix = voice1[i]+voice2[i]+voice3[i];
                output[i]=(byte)(mix/2);
            }
            return output;
        }      
        
    }
    
    
    
    
    private void openSound()
    {
        AudioFormat pcm = new AudioFormat(OUTPUT_SAMPLE_RATE, 8, 1, true, false);
        DataLine.Info info=new DataLine.Info(SourceDataLine.class,pcm);

        try
        {
            line = (SourceDataLine) AudioSystem.getLine(info);
            
            try
            {  
                line.open(pcm);
                int framesize = pcm.getFrameSize( );// 1 byte/sample
                
              //  sampleSize = 1 * 735 * framesize;
                
                line.start( );
            }
            catch (LineUnavailableException ex)
            {
                Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (LineUnavailableException ex)
        {
            Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // generate and play next frame (1/60th of a second) of sound data
    public void playSound()
    {
        if(!status.isSoundEnabled())
            return;

        int srcFrameSize = SOURCE_SAMPLE_RATE/FRAMES_PER_SECOND;// pacman sound runs at 96kHz
        int dstFrameSize = OUTPUT_SAMPLE_RATE/FRAMES_PER_SECOND;// emulator sound runs at 44.1kHz

        byte[] buffer = voiceParameters.generateSoundFrame(srcFrameSize);

        // downsample 96kHz frame to 44.1kHz
        byte[] output = new byte[dstFrameSize];

        // apply antialiasing filter
        buffer = firFilter.filter(buffer);
        
        // resample 96kHz buffer at 44.1kHz
        float inc = (float)srcFrameSize/(float)dstFrameSize;
        for(int i=0;i<dstFrameSize;i++)
        {
            int offset = (int)(inc*(float)i);
            output[i]=buffer[offset];
        }
        
        // write data to sound output
        line.write(output, 0, output.length);
    }
}
