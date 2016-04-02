package phoenix.idex;

import java.util.ArrayList;

/**
 * Created by Ravinder on 3/22/16.
 */
public class ArrayListTools {
    private ArrayList<Integer> fillArrayList = new ArrayList<>();
    private ArrayList<Integer> killArrayList = new ArrayList<>();
    private ArrayList<Integer> sortedFillArray = new ArrayList<>();
    private ArrayList<Integer> sortedKillArray = new ArrayList<>();

    public ArrayListTools(ArrayList<Integer> fillArrayList, ArrayList<Integer> killArrayList) {
        this.fillArrayList = fillArrayList;
        this.killArrayList = killArrayList;
    }

    public void sortFillArrayList() {

        int today = DateColumn.getRowNumber();

        if (today < fillArrayList.size()) {
            int y = 0;
            for (int i = today; i < fillArrayList.size(); i++) {
                sortedFillArray.add(y, fillArrayList.get(i));
                y++;
            }
            for (int i = 0; i < today; i++) {
                sortedFillArray.add(y, fillArrayList.get(i));
                y++;
            }
        } else {
            sortedFillArray = fillArrayList;
        }
    }

    public void sortKillArrayList() {

        int today = DateColumn.getRowNumber();

        if (today < killArrayList.size()) {
            int y = 0;
            for (int i = today; i < killArrayList.size(); i++) {
                sortedKillArray.add(y, killArrayList.get(i));
                y++;
            }
            for (int i = 0; i < today; i++) {
                sortedKillArray.add(y, killArrayList.get(i));
                y++;
            }
        } else {
            sortedKillArray = killArrayList;
        }
    }

    public void sumSortedFillArray() {

        for (int i = 1;  i < sortedFillArray.size(); i++) {
            sortedFillArray.set(i, sortedFillArray.get(i) + sortedFillArray.get(i - 1));
        }
    }

    public void sumSortedKillArray() {

        for (int i = 1;  i < sortedKillArray.size(); i++) {
            sortedKillArray.set(i, sortedKillArray.get(i) + sortedKillArray.get(i - 1));
        }
    }

    public ArrayList<Integer> getGraphList() {
        ArrayList<Integer> trendList = new ArrayList<>();
        for (int i = 0; i < sortedFillArray.size(); i++) {
            trendList.add((sortedFillArray.get(i) * 2) - sortedKillArray.get(i));
        }
        return trendList;
    }
}
