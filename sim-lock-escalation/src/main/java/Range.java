public record Range(Integer start, Integer end) {

    public int size() {
        return end - start + 1;
    }


    public boolean contains(Key key) {
        return start <= key.getId() && end >= key.getId();
    }

    @Override
    public String toString() {
        return "Range{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}

