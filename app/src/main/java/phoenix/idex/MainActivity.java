package phoenix.idex;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.Fragments.AboutFragment;
import phoenix.idex.Fragments.LoginActivityFragment;
import phoenix.idex.Fragments.PostListFragment;
import phoenix.idex.Fragments.TabFragment;
import phoenix.idex.SlidingDrawer.ItemSlideMenu;
import phoenix.idex.SlidingDrawer.SlidingMenuAdapter;

public class MainActivity extends AppCompatActivity {

    private List<ItemSlideMenu> itemList;
    private List<Fragment> fragmentList;
    private SlidingMenuAdapter adapter;
    public static ListView listView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private int currentPos;
    private Toolbar toolbar;
    public static RelativeLayout rLayoutMain;
    private FragmentManager fragmentManager;
    public static boolean isMainShown = false;
    private UserLocalStore userLocalStore;
    private int sizeOfToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slidingdrawer);
        userLocalStore = new UserLocalStore(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragmentManager = getSupportFragmentManager();
        listView = (ListView) findViewById(R.id.listViewMain);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayoutMain);
        rLayoutMain = (RelativeLayout) findViewById(R.id.rLayoutMain);

        sizeOfToolBar = getThemeAttributeDimensionSize(this, R.attr.actionBarSize);

        if (!UserLocalStore.isUserLoggedIn) {
            System.out.println("SDFSDFS");
            setUpDrawerList();
            setUpFragments();
            screenStartUpState();
            drawerListViewListener();
            toggleListener();
        } else {
            setUpLoggedInDrawerList();
            setUpLoggedInFragments();
            screenStartUpState();
            drawerListViewLoggedInUserListener();
            toggleListener();
        }
    }

    public static int getThemeAttributeDimensionSize(Context context, int attr)
    {
        TypedArray a = null;
        try{
            a = context.getTheme().obtainStyledAttributes(new int[] { attr });
            return a.getDimensionPixelSize(0, 0);
        }finally{
            if(a != null){
                a.recycle();
            }
        }
    }

    // Listens to when drawer navigation is opened or closed.
    // Disabled menu items on action bar
    private void toggleListener() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawerOpened, R.string.drawerClosed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
    }

    // Add items to the drawer list for navigation drawer
    private void setUpDrawerList() {
        itemList = new ArrayList<>();

        itemList.add(new ItemSlideMenu("Log In"));
        itemList.add(new ItemSlideMenu("Roll"));
        itemList.add(new ItemSlideMenu("About"));

        adapter = new SlidingMenuAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    // Set up the fragments
    private void setUpFragments() {
        fragmentList = new ArrayList<>();

        fragmentList.add(new LoginActivityFragment());
        fragmentList.add(new PostListFragment());
        fragmentList.add(new AboutFragment());
    }

    // Add items to the drawer list for navigation drawer
    private void setUpLoggedInDrawerList() {
        itemList = new ArrayList<>();

        itemList.add(new ItemSlideMenu("Roll"));
        itemList.add(new ItemSlideMenu("Profile"));
        itemList.add(new ItemSlideMenu("Log Out"));
        itemList.add(new ItemSlideMenu("About"));

        adapter = new SlidingMenuAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    // Set up the fragments
    private void setUpLoggedInFragments() {
        fragmentList = new ArrayList<>();

        fragmentList.add(new PostListFragment());
        fragmentList.add(new TabFragment());
        fragmentList.add(new PostListFragment());
        fragmentList.add(new AboutFragment());
    }

    // Start up state
    //Title Idex, with closed navigation drawer and default fragment 1
    private void screenStartUpState() {
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // rLayoutMain.setPadding(0,0,0,0);
        isMainShown = true;
        setTitle("");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.rLayoutMain, fragmentList.get(0)).addToBackStack(null).commit();
        listView.setItemChecked(0, true);
        drawerLayout.closeDrawer(listView);
    }

    // Fragments to be displayed based on user selection from navigation drawer
    private void drawerListViewLoggedInUserListener() {
        // Handle click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPos = position;

                // Sign out option
                if (position == 2) {
                    currentPos = 0;
                    Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    // Erase local history of logged in user
                    userLocalStore.clearUserData();
                    UserLocalStore.visitCounter = 0;
                    UserLocalStore.isUserLoggedIn = false;
                    setUpDrawerList();
                    setUpFragments();
                    screenStartUpState();
                    drawerListViewListener();
                } else {
                    if (position == 0) {
                        isMainShown = true;
                        setTitle("");
                    } else {
                        isMainShown = false;
                        setTitle(itemList.get(position).getTitle());
                    }
                    drawerLayout.closeDrawer(listView);
                    // Set item to selected
                    listView.setItemChecked(position, true);
                }
                // Delay to avoid lag between navigation drawer items
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                                fragmentList.get(currentPos)).commit();
                    }
                }, 270);
            }
        });
    }

    // Fragments to be displayed based on user selection from navigation drawer
    private void drawerListViewListener() {
        // Handle click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPos = position;
                if (position == 0) {
                    isMainShown = true;
                    setTitle("");
                } else {
                    isMainShown = false;
                    setTitle(itemList.get(position).getTitle());
                }
                drawerLayout.closeDrawer(listView);
                // Set item to selected
                listView.setItemChecked(position, true);

                // Delay to avoid lag between navigation drawer items
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                                fragmentList.get(currentPos)).commit();
                    }
                }, 270);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync it based on if navigation drawer is selected or not.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (isMainShown) {
            finish();
        } else {
            listView.setItemChecked(0, true);
            setTitle(itemList.get(0).getTitle());
            fragmentManager.beginTransaction().replace(R.id.rLayoutMain, fragmentList.get(0)).commit();
            isMainShown = true;
        }
    }
}
