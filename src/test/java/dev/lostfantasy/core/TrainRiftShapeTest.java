package dev.lostfantasy.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class TrainRiftShapeTest {
    @Test public void entireTrainFrontFitsThroughOpening() {
        for(double y=0;y<=CastMotion.TRAIN_HEIGHT;y+=.025)
            assertTrue("Opening clips the train at height "+y,TrainRiftShape.widthAt(y)>CastMotion.TRAIN_HALF_WIDTH+.1);
    }
    @Test public void spindleClosesAtBothTipsAndHasNoImaginaryWidthOutside() {
        assertEquals(0,TrainRiftShape.widthAt(TrainRiftShape.CENTER-TrainRiftShape.HALF_HEIGHT),0);
        assertEquals(0,TrainRiftShape.widthAt(TrainRiftShape.CENTER+TrainRiftShape.HALF_HEIGHT),0);
        assertEquals(0,TrainRiftShape.widthAt(100),0);
        assertEquals(0,TrainRiftShape.widthAt(-100),0);
    }
}
