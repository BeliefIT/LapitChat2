package solutionexpert.org.lapitchat2;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mtoolbar;
    private ViewPager viewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mtoolbar=findViewById(R.id.mainpage_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("lapit chat");

        viewPager=findViewById(R.id.main_tabPager);

        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout=findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
       // updateUI(currentUser);

        if (currentUser==null)
        {
            sendToStartActivity();
        }
    }

    private void sendToStartActivity() {
        Intent startIntent=new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.main_logout_btn)
        {
            FirebaseAuth.getInstance().signOut();
            sendToStartActivity();
        }
        if (item.getItemId()==R.id.account_settings_btn)
        {
            Intent settingsIntent=new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return true;
    }
}
