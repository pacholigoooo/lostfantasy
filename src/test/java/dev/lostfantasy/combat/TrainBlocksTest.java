package dev.lostfantasy.combat;
import dev.lostfantasy.Balance;
import java.util.HashSet;
import org.junit.Test;
public class TrainBlocksTest {
    @Test public void disabledDestructionDoesNotEvenAccessTheWorld() {
        boolean old=Balance.trainBreakBlocks;
        try {Balance.trainBreakBlocks=false;TrainBlocks.sweep(null,null,null,0,100,new HashSet<>(),()->true);}
        finally {Balance.trainBreakBlocks=old;}
    }
    @Test public void interruptedDestructionDoesNotAccessTheOwnersCurrentWorld() {
        boolean old=Balance.trainBreakBlocks;
        try {Balance.trainBreakBlocks=true;TrainBlocks.sweep(null,null,null,0,100,new HashSet<>(),()->false);}
        finally {Balance.trainBreakBlocks=old;}
    }
}
