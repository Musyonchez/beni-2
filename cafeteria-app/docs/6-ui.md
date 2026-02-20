# 6 — UI Design Guidelines

## Branding
| Token    | Value     | Use                                      |
|----------|-----------|------------------------------------------|
| Primary  | #002147   | App bar, buttons, active nav tab, FAB    |
| Secondary| #CFB991   | Accent highlights, chips, badges         |
| On-Primary | #FFFFFF | Text/icons on primary colour             |
| Background | #FAFAFA | Screen background                        |
| Surface  | #FFFFFF   | Cards, bottom sheets                     |
| Error    | #B00020   | Error states                             |

## Design system
- **Material Design 3** — use Material 3 components throughout
- Apply theme via `res/values/themes.xml`:
  ```xml
  <style name="Theme.CafeteriaApp" parent="Theme.Material3.Light.NoActionBar">
      <item name="colorPrimary">#002147</item>
      <item name="colorSecondary">#CFB991</item>
      <item name="colorPrimaryVariant">#001530</item>
      <item name="colorSecondaryVariant">#B8A07A</item>
  </style>
  ```

## Navigation
- `BottomNavigationView` with 5 items for students:
  - Menu (ic_restaurant), Cart (ic_shopping_cart) + badge, Orders (ic_receipt), Pre-order (ic_event), Profile (ic_account_circle)
- Staff has its own bottom nav with 3 items:
  - Orders (ic_receipt), Menu (ic_restaurant), Wallet (ic_account_balance_wallet)
- Fragment switching uses **hide/show** not replace:
  ```java
  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
  ft.hide(currentFragment);
  ft.show(targetFragment);
  ft.commit();
  ```
- If a fragment has not been added yet, use `ft.add()` first

## Typography
Use Material 3 default typography (Roboto). Do not introduce custom fonts unless specifically required.
- Screen titles: `textAppearanceTitleLarge`
- Card content: `textAppearanceBodyMedium`
- Price / balance: `textAppearanceHeadlineMedium` (bold)
- Labels / chips: `textAppearanceLabelMedium`

## Key components

### Menu item card (item_menu.xml)
- MaterialCardView with 8dp corner radius, 2dp elevation
- Left: image placeholder (ImageView, 80×80dp, rounded corners via Glide)
- Right: name (bold), description (2 lines max, ellipsize), price
- Bottom-right: MaterialButton "Add to Cart" (filled, primary colour)
- If unavailable: overlay chip "Unavailable" (amber) + button disabled

### Cart item row (item_cart.xml)
- Item name + price per unit (left)
- Quantity stepper: IconButton − | TextView count | IconButton + (right)
- Line total below item name
- Trash icon at far right

### Order card (item_order.xml)
- Status chip (colour-coded: amber=Pending, blue=Preparing, green=Ready, grey=Collected)
- Item names list (truncated to 2 lines)
- Total amount + time placed
- Tap to expand full item list

### Pre-order card (item_preorder.xml)
- Meal slot tag + date
- Status chip
- Items summary + total

### Wallet transaction row (item_wallet_tx.xml)
- Icon: arrow_upward (green tint) for top-up, arrow_downward (red tint) for deduction
- Description text
- Amount: "+KES X" (green) or "-KES X" (red)
- Date/time (small, secondary text)

## Status chip colour mapping
```java
switch (status) {
    case "pending":    chip.setChipBackgroundColorResource(R.color.amber_100); break;
    case "preparing":  chip.setChipBackgroundColorResource(R.color.blue_100);  break;
    case "ready":      chip.setChipBackgroundColorResource(R.color.green_100); break;
    case "collected":  chip.setChipBackgroundColorResource(R.color.grey_100);  break;
    case "scheduled":  chip.setChipBackgroundColorResource(R.color.grey_100);  break;
    case "confirmed":  chip.setChipBackgroundColorResource(R.color.green_100); break;
    case "cancelled":  chip.setChipBackgroundColorResource(R.color.red_100);   break;
}
```
Define these colours in `res/values/colors.xml`.

## Spacing and sizing
- Screen horizontal padding: 16dp
- Card margin between items: 8dp
- Button height: 48dp (Material 3 default)
- Bottom navigation height: 80dp (default MD3)
- FAB position: bottom-right, 16dp margin

## Empty states
- When a list is empty (no orders, no cart items, no pre-orders), show:
  - Centred icon (128dp, grey tint)
  - Short message: "No orders yet", "Your cart is empty", etc.
  - Optional action button where relevant (e.g. "Browse Menu" on empty cart)

## Error / loading states
- Use `CircularProgressIndicator` centred on the screen while data loads
- On Firestore error, show a `Snackbar` with a "Retry" action
- Never show raw exception messages to the user — use friendly strings from `strings.xml`

## Accessibility
- All ImageViews must have `contentDescription`
- Interactive elements must have minimum 48dp touch target
- Colour is never the only differentiator — always pair colour with text or icon
