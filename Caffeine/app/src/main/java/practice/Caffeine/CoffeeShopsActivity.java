package practice.Caffeine;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CoffeeShopsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShopAdapter adapter;
    private ArrayList<ShopCard> shopList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_shops);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        shopList = new ArrayList<>();
        adapter = new ShopAdapter(this, shopList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareShops();

        try {
            Glide.with(this).load(R.drawable.love_coffee).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }

         // Variables passed to this activity from Login
         Intent intent = getIntent();
         final String name = intent.getStringExtra("name");
        final Integer userID = intent.getIntExtra("userID", 0);
         final Button bAddShop = (Button) findViewById(R.id.bAddShop);


         // Button to move to new coffee shop activity with variables
         bAddShop.setOnClickListener(new View.OnClickListener() {

             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(CoffeeShopsActivity.this, NewCoffeeShopActivity.class);
                 intent.putExtra("userID", userID);
                 intent.putExtra("name", name);
                 CoffeeShopsActivity.this.startActivity(intent);
             }
         });

     }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * Adding ShopCards
     */
    private void prepareShops() {
        int[] shopImages = new int[]{
                R.drawable.addshop,
                R.drawable.shop1,
                R.drawable.shop2,
                R.drawable.shop3,
                R.drawable.shop4,
                R.drawable.shop5,
                R.drawable.shop6,
        };

        ShopCard a = new ShopCard("Add New Coffee Shop", 0, shopImages[0]);
        shopList.add(a);

        DatabaseHelper myDB = new DatabaseHelper(this);
        myDB.getReadableDatabase();
        List<Shop> dbShopList = myDB.getAllShops();
        Log.d("DBshopList: ", dbShopList.toString());

        Random rand = new Random();

        if (dbShopList.size() > 0) {
            for (int i = 1; i <= dbShopList.size(); i++) {
                ShopCard shopCard = new ShopCard();
                Shop shop = new Shop();
                shop = myDB.getShop(i);
                shopCard.setName(shop.getName());
                int numVisits = 0;
                Integer shopId = myDB.getShop(i).getShopID();
                for (int j = 0; j < myDB.getAllVisits().size(); j++) {
                    if (myDB.getVisit(i).getShopID() == shopId) {
                        numVisits++;
                    }
                }
                shopCard.setNumOfVisits(numVisits);
                int x = 1 + rand.nextInt(5);
                shopCard.setShopImage(shopImages[x]);
                shopList.add(shopCard);
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

}
