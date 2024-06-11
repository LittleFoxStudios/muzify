package com.littlefoxstudios.muzify.homescreenfragments.history.outercard;

public interface CardInterface {
    void onItemClick(Integer position);
    void itemSelected(int position);
    boolean isDeleteModeSelected();
    void handleLongClick(int position);
}
