package com.example.martinhuang.mapgps;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

public class SearchLocationActivity extends AppCompatActivity {

    public static final String CENTER_X = "CENTER_X";
    public static final String CENTER_Y = "CENTER_Y";

    private static final int COLOR_TRANSITION_TIME = 500;

    private int revealAnimationCX;
    private int revealAnimationCY;

    protected Toolbar toolbar;
    RecyclerView recyclerView;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        toolbar = (Toolbar)findViewById(R.id.search_toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.colorBlue));
        changeStatusBarColor(ContextCompat.getColor(this,R.color.colorBlue));

        recyclerView = (RecyclerView) findViewById(R.id.search_results);

        revealTransition();
        initToolbar();
        setupSearchView();

    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @TargetApi(21)
    private void changeStatusBarColor(int color) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            this.getWindow().setStatusBarColor(color);
        }
    }

    @TargetApi(21)
    protected void revealTransition(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = getIntent();
            revealAnimationCX = intent.getIntExtra(CENTER_X,0);
            revealAnimationCY = intent.getIntExtra(CENTER_Y, 0);

            RevealTransition revealTransition = new RevealTransition(revealAnimationCX, revealAnimationCY);

            revealTransition.excludeTarget(android.R.id.navigationBarBackground,true);

            Animator.AnimatorListener appearAnimationListener = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(recyclerView != null) {
                        TransitionDrawable transition = (TransitionDrawable) recyclerView.getBackground();
                        transition.startTransition(COLOR_TRANSITION_TIME);
                    }

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if(recyclerView != null) {
                        TransitionDrawable transition = (TransitionDrawable) recyclerView.getBackground();
                        transition.startTransition(COLOR_TRANSITION_TIME);
                    }


                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            };
            revealTransition.setOnAppearAnimationListener(appearAnimationListener);
            getWindow().setEnterTransition(revealTransition);
        }
    }

    protected void setupSearchView() {
        /* Show the soft keyboard */
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                performNewSearch(query);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//        searchView.clearFocus();
    }
}
