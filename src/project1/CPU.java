package project1;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class CPU {
    /*
        Declare required registers
    */
    static int PC = 0, SP = 1000, IR, AC, X, Y, timerFlag, instruction_count = 0;
    static int systemStack_top = 2000, userStack_top = 1000;
    
    static boolean userMode = true; // initially set it to true. 
                                    // On interrupt set it to false to indicate 
                                    // kernel mode
    static boolean processingInterrupt = false; // flag to avoid nested interrupt execution
    
    public static void main(String args[]) {
        
        String fileName = null;
        
        // check the command line argument length
        if(args.length == 2) {
             fileName = args[0];
             timerFlag = Integer.parseInt(args[1]); // set timer interrupt value
        }
        else {  // if incorrect number of parameters then exit
            System.out.println("Incorrect number of parameters. Process ended.");
            System.exit(0);
        }

        try {            
            /*
                Create child process and set up I/O streams
            */
            Runtime rt = Runtime.getRuntime();

            Process proc = rt.exec("java Memory");

            OutputStream os = proc.getOutputStream();
            PrintWriter pw = new PrintWriter(os);

            InputStream is = proc.getInputStream();
            Scanner memory_reader = new Scanner(is); // direct input stream to a Scanner object
            
            // Send file name to child process
            fileNameToMemory(pw, is, os, fileName);
            
            // this loop will keep the communication going between CPU and memory.
            while (true) {
                
                // check to see if timer interrupt has occurred.
                if(instruction_count > 0 && (instruction_count % timerFlag) == 0 
                		&& processingInterrupt == false) {
                    // process the interrupt
                    processingInterrupt = true;
                    interruptFromTimer(pw, is, memory_reader, os);
                }
                
                // read instruction from memory
                int value = readFromMemory(pw, is, memory_reader, os, PC);
                
                if (value != -1)
                	processInstruction(value, pw, is, memory_reader, os);
                else
                    break;
            }
            
            proc.waitFor();
            int exitVal = proc.exitValue();
            System.out.println("Process exited: " + exitVal);
        } 
        catch (IOException | InterruptedException t) {
           t.printStackTrace();
        }

    }

    /*
        function to send file name to memory
    */
    private static void fileNameToMemory(PrintWriter pw, InputStream is, 
    		OutputStream os, String fileName) {
        pw.printf(fileName + "\n");  //send filename to memory
        pw.flush();
    }

    // function to read data at given address from memory
    private static int readFromMemory(PrintWriter pw, InputStream is, Scanner memory_reader, 
    		OutputStream os, int address) {
        checkMemoryViolation(address);
        pw.printf("1," + address + "\n");
        pw.flush();
        if (memory_reader.hasNext()) {
            String temp = memory_reader.next();
            if(!temp.isEmpty()) {
                int temp2 = Integer.parseInt(temp);
                return (temp2); 
            }
        }
        return -1;
    }
    
    //function to tell child process to write data at the given address in memory
    private static void writeToMemory(PrintWriter pw, InputStream is,
    		OutputStream os, int address, int data) {
        pw.printf("2," + address + "," + data + "\n"); //2 at the start on string indicates write
        pw.flush();
    }

    // function to process an instruction received from the memory
    private static void processInstruction(int value, PrintWriter pw, InputStream is, 
    		Scanner memory_reader, OutputStream os) {
        IR = value; //store instruction in Instruction register
        int operand;    //to store operand
        
        switch(IR) {
        
            case 1: //Load the value into the AC
                PC++; // increment counter to get operand
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                AC = operand;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 2: // Load the value at the address into the AC
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                AC = readFromMemory(pw, is, memory_reader, os, operand);
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;

            case 3: // Load the value from the address found in the address into the AC
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                operand = readFromMemory(pw, is, memory_reader, os, operand);
                AC = readFromMemory(pw, is, memory_reader, os, operand);
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
                
            case 4: // Load the value at (address+X) into the AC
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                AC = readFromMemory(pw, is, memory_reader, os, operand + X);
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 5: //Load the value at (address+Y) into the AC
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                AC = readFromMemory(pw, is, memory_reader, os, operand + Y);
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 6: //Load from (Sp+X) into the AC
                AC = readFromMemory(pw, is, memory_reader, os, SP + X);
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 7: //Store the value in the AC into the address
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                writeToMemory(pw, is, os, operand, AC);
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 8: //Gets a random int from 1 to 100 into the AC
                Random r = new Random();
                int i = r.nextInt(100) + 1;
                AC = i;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 9: //If port=1, writes AC as an int to the screen
                    //If port=2, writes AC as a char to the screen
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                if(operand == 1) {
                    System.out.print(AC);
                    if(processingInterrupt == false) 
                        instruction_count++;
                    PC++;
                    break;

                }
                else if (operand == 2) {
                    System.out.print((char)AC);
                    if(processingInterrupt == false) 
                        instruction_count++;
                    PC++;
                    break;
                }
                else {
                    System.out.println("Error: Port = " + operand);
                    if(processingInterrupt == false) 
                        instruction_count++;
                    PC++;
                    System.exit(0);
                    break;
                }
                
            case 10: // Add the value in X to the AC
                AC = AC + X;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 11: //Add the value in Y to the AC
                AC = AC + Y;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 12: //Subtract the value in X from the AC
                AC = AC - X;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 13: //Subtract the value in Y from the AC
                AC = AC - Y;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 14: //Copy the value in the AC to X
                X = AC;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 15: //Copy the value in X to the AC
                AC = X;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 16: //Copy the value in the AC to Y
                Y = AC;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 17: //Copy the value in Y to the AC
                AC = Y;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 18: //Copy the value in AC to the SP
                SP = AC;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 19: //Copy the value in SP to the AC 
                AC = SP;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 20: // Jump to the address
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                PC = operand;
                if(processingInterrupt == false) 
                    instruction_count++;
                break;
                
            case 21: // Jump to the address only if the value in the AC is zero
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                if (AC == 0) 
                {
                    PC = operand;
                    if(processingInterrupt == false) 
                        instruction_count++;
                    break;
                }
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 22: // Jump to the address only if the value in the AC is not zero
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                if (AC != 0) 
                {
                    PC = operand;
                    if(processingInterrupt == false) 
                        instruction_count++;
                    break;
                }
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
                
            case 23: // Push return address onto stack, jump to the address
                PC++;
                operand = readFromMemory(pw, is, memory_reader, os, PC);
                pushValueToStack(pw, is, os,PC+1);
                userStack_top = SP;
                PC = operand;
                if(processingInterrupt == false) 
                    instruction_count++;
                break;
                
            case 24: // Pop return address from the stack, jump to the address
                operand = popValueFromStack(pw, is, memory_reader, os);
                PC = operand;
                if(processingInterrupt == false) 
                    instruction_count++;
                break;
                
            case 25: // Increment the value in X
                X++;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
            
            case 26: // Decrement the value in X
                X--;
                if(processingInterrupt == false) 
                    instruction_count++;
                PC++;
                break;
            
            case 27: // Push AC onto stack
                pushValueToStack(pw, is, os,AC);
                PC++;
                if(processingInterrupt == false) 
                    instruction_count++;
                break;
                
            case 28: // Pop from stack into AC
                AC = popValueFromStack(pw, is, memory_reader, os);
                PC++;
                if(processingInterrupt == false) 
                    instruction_count++;
                break;
                
            case 29: // Int call. Set system mode, switch stack, push SP and PC, set new SP and PC
                processingInterrupt = true;
                userMode = false;
                operand = SP;
                SP = 2000;
                pushValueToStack(pw, is, os, operand);
                
                operand = PC + 1;
                PC = 1500;
                pushValueToStack(pw, is, os, operand);
                
                if(processingInterrupt == false) 
                    instruction_count++;
                
                break;
                
            case 30: // Restore registers, set user mode
                
                PC = popValueFromStack(pw, is, memory_reader, os);
                SP = popValueFromStack(pw, is, memory_reader, os);
                userMode = true;
                instruction_count++;
                processingInterrupt = false;
                break;
                
            case 50: // End Execution
                if(processingInterrupt == false) 
                    instruction_count++;
                System.exit(0);
                break;
            
            default:
                System.out.println("Unknown error - default");
                System.exit(0);
        }
    }

    // function to check if user program if trying to access system memory and stack
    private static void checkMemoryViolation(int address) 
    {
        if(userMode && address > 1000)
        {
            System.out.println("Error: User tried to access system stack. Process exiting.");
            System.exit(0);
        }
    }

    // function to handle interrupts caused by the timer
    private static void interruptFromTimer(PrintWriter pw, InputStream is, Scanner memory_reader, OutputStream os) 
    {
        int operand;
        userMode = false;
        operand = SP;
        SP = systemStack_top;
        pushValueToStack(pw, is, os, operand);

        operand = PC;
        PC = 1000;
        pushValueToStack(pw, is, os, operand);
    }

    // function to push a value to the appropriate stack
    private static void pushValueToStack(PrintWriter pw, InputStream is, OutputStream os, int value) 
    {
        SP--;
        writeToMemory(pw, is, os, SP, value);
    }

    // function to pop a value from the appropriate stack
    private static int popValueFromStack(PrintWriter pw, InputStream is, Scanner memory_reader, OutputStream os) 
    {
        int temp = readFromMemory(pw, is, memory_reader, os, SP);
        writeToMemory(pw, is, os, SP, 0);
        SP++;
        return temp;
    }
}
