# CPU-Memory-SImulation

The project will simulate a simple computer system consisting of a CPU and Memory.
The CPU and Memory will be simulated by separate processes that communicate.
Memory will contain one program that the CPU will execute and then the simulation will end.

Instruction set

    1 = Load value              Load the value into the AC             
    2 = Load addr               Load the value at the address into the AC
    3 = LoadInd addr            Load the value from the address found in the given address into the AC
                                (for example, if LoadInd 500, and 500 contains 100, then load from 100).
    4 = LoadIdxX addr           Load the value at (address+X) into the AC
                                (for example, if LoadIdxX 500, and X contains 10, then load from 510).
    5 = LoadIdxY addr           Load the value at (address+Y) into the AC
    6 = LoadSpX                 Load from (Sp+X) into the AC (if SP is 990, and X is 1, load from 991).
    7 = Store addr              Store the value in the AC into the address
    8 = Get                     Gets a random int from 1 to 100 into the AC
    9 = Put port                If port=1, writes AC as an int to the screen
                                If port=2, writes AC as a char to the screen
   10 = AddX                    Add the value in X to the AC
   11 = AddY                    Add the value in Y to the AC
   12 = SubX                    Subtract the value in X from the AC
   13 = SubY                    Subtract the value in Y from the AC
   14 = CopyToX                 Copy the value in the AC to X
   15 = CopyFromX               Copy the value in X to the AC
   16 = CopyToY                 Copy the value in the AC to Y
   17 = CopyFromY               Copy the value in Y to the AC
   18 = CopyToSp                Copy the value in AC to the SP
   19 = CopyFromSp              Copy the value in SP to the AC 
   20 = Jump addr               Jump to the address
   21 = JumpIfEqual addr        Jump to the address only if the value in the AC is zero
   22 = JumpIfNotEqual addr     Jump to the address only if the value in the AC is not zero
   23 = Call addr               Push return address onto stack, jump to the address
   24 = Ret                     Pop return address from the stack, jump to the address
   25 = IncX                    Increment the value in X
   26 = DecX                    Decrement the value in X
   27 = Push                    Push AC onto stack
   28 = Pop                     Pop from stack into AC
   29 = Int                     Perform system call
   30 = IRet                    Return from system call
   50 = End                     End execution
