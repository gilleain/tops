package python.model;

public class LinearLayout {
    
    public void layout(Cartoon cartoon) {
        int index = 0;
        for (SSE sse : cartoon.getSSEs()) {
            sse.setPosition(index * 50, 50);
        }
    }

}
