/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.client.ui.swing.lobby;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.*;

import megamek.client.ui.Messages;
import megamek.client.ui.swing.ClientGUI;
import megamek.client.ui.swing.tooltip.PilotToolTip;
import megamek.client.ui.swing.tooltip.UnitToolTip;
import megamek.client.ui.swing.util.UIUtil;
import megamek.common.*;
import megamek.common.annotations.Nullable;
import megamek.common.icons.Camouflage;
import megamek.common.icons.Portrait;
import megamek.common.options.*;
import megamek.common.util.fileUtils.MegaMekFile;

import static megamek.client.ui.swing.util.UIUtil.*;

public class MekTableModel extends AbstractTableModel {
    //region Variable Declarations
    private static final long serialVersionUID = 4819661751806908535L;

    private enum COLS { UNIT, PILOT, PLAYER, BV }

    public static final int COL_UNIT = COLS.UNIT.ordinal();
    public static final int COL_PILOT = COLS.PILOT.ordinal();
    public static final int COL_PLAYER = COLS.PLAYER.ordinal();
    public static final int COL_BV = COLS.BV.ordinal();
    public static final int N_COL = COLS.values().length;
    
    // Some unicode symbols. These work on Windows when setting the font 
    // to Dialog (which I believe uses Arial). I hope they work on other systems.
    public static final String DOT_SPACER = " \u2B1D ";

    /** Control value for the size of camo and portraits in the table at GUI scale == 1. */
    static final int MEKTABLE_IMGHEIGHT = 60;

    private static final String UNKNOWN_UNIT = new MegaMekFile(Configuration.miscImagesDir(),
            "unknown_unit.gif").toString();
    private static final String DEF_PORTRAIT = new MegaMekFile(Configuration.portraitImagesDir(),
            Portrait.DEFAULT_PORTRAIT_FILENAME).toString();

    // Parent access
    private ClientGUI clientGui;
    private ChatLounge chatLounge;

    /** The displayed entities. This list is the actual table data. */
    private ArrayList<Entity> entities = new ArrayList<>();
    /** The contents of the battle value column. Gets formatted for display (font scaling). */
    private ArrayList<Integer> bv = new ArrayList<>();
    /** The displayed contents of the Unit column. */
    private ArrayList<String> unitCells = new ArrayList<>();
    /** The displayed contents of the Pilot column. */
    private ArrayList<String> pilotCells = new ArrayList<>();
    /** The list of cached tooltips for the displayed units. */
    private ArrayList<String> unitTooltips = new ArrayList<>();
    /** The list of cached tooltips for the displayed pilots. */
    private ArrayList<String> pilotTooltips = new ArrayList<>();
    /** The displayed contents of the Player column. */
    private ArrayList<String> playerCells = new ArrayList<>();
    //endregion Variable Declarations

    //region Constructors
    public MekTableModel(ClientGUI cg, ChatLounge cl) {
        clientGui = cg;
        chatLounge = cl;
    }
    //endregion Constructors

    @Override
    public Object getValueAt(int row, int col) {
        final Entity entity = entities.get(row);
        if (entity == null) {
            return "Error: Unit not found";
        }

        if (col == COLS.BV.ordinal()) {
            boolean isEnemy = clientGui.getClient().getLocalPlayer().isEnemyOf(ownerOf(entity));
            boolean isBlindDrop = clientGui.getClient().getGame().getOptions().booleanOption(OptionsConstants.BASE_BLIND_DROP);
            boolean hideEntity = isEnemy && isBlindDrop;
            float size = chatLounge.isCompact() ? 0 : 0.2f;
            return hideEntity ? "" : guiScaledFontHTML(size) + NumberFormat.getIntegerInstance().format(bv.get(row));
            
        } else if (col == COLS.PLAYER.ordinal()) {
             return playerCells.get(row);
             
        } else if (col == COLS.PILOT.ordinal()) {
            return pilotCells.get(row);
            
        } else if (col == COLS.UNIT.ordinal()) {
            return unitCells.get(row);
            
        } else { 
            return "";
        }
    }

    @Override
    public int getRowCount() {
        return entities.size();
    }

    /** Clears all saved data of the model including the entities. */
    public void clearData() {
        entities.clear();
        bv.clear();
        unitTooltips.clear(); 
        pilotTooltips.clear();
        unitCells.clear();
        pilotCells.clear();
        playerCells.clear();
        fireTableDataChanged();
    }
    
    /** 
     * Rebuilds the display content of the table cells from the present entity list.
     * Used when the GUI scale changes. 
     */
    public void refreshCells() {
        bv.clear();
        unitTooltips.clear(); 
        pilotTooltips.clear();
        playerCells.clear();
        unitCells.clear();
        pilotCells.clear();
        for (Entity entity: entities) {
            addCellData(entity);
        }
        fireTableDataChanged();
    }

    /** Adds the given entity to the table and builds the display content. */
    public void addUnit(Entity entity) {
        entities.add(entity);
        addCellData(entity);
        fireTableDataChanged();
    }

    /** 
     * Adds display content for the given entity.
     * The entity is assumed to be the last entity added to the table and 
     * the display content will be added as a new last table row. 
     */  
    private void addCellData(Entity entity) {
        bv.add(entity.calculateBattleValue());
        playerCells.add(playerCellContent(entity));

        IPlayer owner = ownerOf(entity);
        // Note that units of a player's bots are obscured because they could be added from
        // a MekHQ AtB campaign. Thus, the player can still configure them and so can identify
        // the obscured units but has to actively decide to do it.
        boolean hideEntity = clientGui.getClient().getLocalPlayer().isEnemyOf(owner)
                && clientGui.getClient().getGame().getOptions().booleanOption(OptionsConstants.BASE_BLIND_DROP);
        if (hideEntity) {
            unitTooltips.add(null);
            pilotTooltips.add(null);
        } else {
            MapSettings mset = chatLounge.mapSettings;
            IPlayer lPlayer = clientGui.getClient().getLocalPlayer();
            unitTooltips.add("<HTML>" + UnitToolTip.getEntityTipLobby(entity, lPlayer, mset));
            pilotTooltips.add("<HTML>" + PilotToolTip.getPilotTipDetailed(entity));
        }
        final boolean rpgSkills = clientGui.getClient().getGame().getOptions().booleanOption(OptionsConstants.RPG_RPG_GUNNERY);
        if (chatLounge.isCompact()) {
            unitCells.add(LobbyMekCellFormatter.formatUnitCompact(entity, chatLounge, false));
            pilotCells.add(LobbyMekCellFormatter.formatPilotCompact(entity, hideEntity, rpgSkills));
        } else {
            unitCells.add(LobbyMekCellFormatter.formatUnitFull(entity, chatLounge, false));
            pilotCells.add(LobbyMekCellFormatter.formatPilotFull(entity, hideEntity));
        }

    }
    
    /** Returns the tooltip for the given row and column from the tooltip cache. */
    public String getTooltip(int row, int col) {
        if (col == COLS.PILOT.ordinal()) {
            return pilotTooltips.get(row);
        } else if (col == COLS.UNIT.ordinal()) {
            return unitTooltips.get(row);
        } else {
            return null;
        }
    }
    
    /** 
     * Returns the column header for the given column. The header text is HTML and 
     * scaled according to the GUI scale. 
     */
    @Override
    public String getColumnName(int column) {
        String result = "<HTML>" + UIUtil.guiScaledFontHTML(0.2f);
        if (column == COLS.PILOT.ordinal()) {
            return result + Messages.getString("ChatLounge.colPilot");
        } else if (column == COLS.UNIT.ordinal()) {
            return result + Messages.getString("ChatLounge.colUnit");
        } else if (column == COLS.PLAYER.ordinal()) {
            return result + Messages.getString("ChatLounge.colPlayer");
        } else if (column == COLS.BV.ordinal()) {
            return result + Messages.getString("ChatLounge.colBV");
        } else {
            return "??";
        }
    }

    /** Returns the owner of the given entity. Prefer this over entity.getOwner(). */
    private IPlayer ownerOf(Entity entity) {
        return clientGui.getClient().getGame().getPlayer(entity.getOwnerId());
    }
    
    /** Creates and returns the display content of the "Player" column for the given entity. */
    private String playerCellContent(final Entity entity) {
        StringBuilder result = new StringBuilder("<HTML><NOBR>");
        IPlayer owner = ownerOf(entity);
        boolean isEnemy = clientGui.getClient().getLocalPlayer().isEnemyOf(owner);
        float size = chatLounge.isCompact() ? 0 : 0.2f;
        String sep = chatLounge.isCompact() ? DOT_SPACER : "<BR>";
        result.append(guiScaledFontHTML(owner.getColour().getColour(), size)).append(owner.getName())
                .append("</FONT>").append(guiScaledFontHTML(size)).append(sep).append("</FONT>")
                .append(guiScaledFontHTML(isEnemy ? Color.RED : uiGreen(), size))
                .append(IPlayer.teamNames[owner.getTeam()]);
        return result.toString();
    }
    
    /** Returns the entity of the given table row. */
    public Entity getEntityAt(int row) {
        return entities.get(row);
    }
    
    public static int columnPilot() {
        return COLS.PILOT.ordinal();
    }
    
    public static int columnBV() {
        return COLS.BV.ordinal();
    }
    
    public static int columnUnit() {
        return COLS.UNIT.ordinal();
    }
    
    public static int columnPlayer() {
        return COLS.PLAYER.ordinal();
    }
    
    /** Returns the subclassed cell renderer for all columns except the force column. */
    public MekTableModel.Renderer getRenderer() {
        return new MekTableModel.Renderer();
    }
    
    /** A specialized renderer for the mek table. */
    public class Renderer extends DefaultTableCellRenderer implements TableCellRenderer {
        
        private static final long serialVersionUID = -9154596036677641620L;
        
        @Override
        public Component getTableCellRendererComponent(final JTable table,
                                                       final @Nullable Object value,
                                                       final boolean isSelected,
                                                       final boolean hasFocus,
                                                       final int row, final int column) {
            final Entity entity = getEntityAt(row);
            if ((entity == null) || (value == null)) {
                return null;
            }

            setIconTextGap(UIUtil.scaleForGUI(10));
            setText("<HTML>" + value.toString());
            boolean compact = chatLounge.isCompact();
            if (compact) {
                setIcon(null);
            }

            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                Color background = table.getBackground();
                if (row % 2 != 0) {
                    background = alternateTableBGColor();
                }
                setBackground(background);
            }

            IPlayer owner = ownerOf(entity);
            boolean showAsUnknown = clientGui.getClient().getLocalPlayer().isEnemyOf(owner)
                    && clientGui.getClient().getGame().getOptions().booleanOption(OptionsConstants.BASE_BLIND_DROP);
            int size = UIUtil.scaleForGUI(MEKTABLE_IMGHEIGHT);

            if (showAsUnknown) {
                setToolTipText(null);
                if (column == COLS.UNIT.ordinal()) {
                    if (!compact) {
                        Image image = getToolkit().getImage(UNKNOWN_UNIT);
                        setIcon(new ImageIcon(image.getScaledInstance(-1, size, Image.SCALE_SMOOTH)));
                    }
                } else if (column == COLS.PILOT.ordinal()) {
                    if (!compact) {
                        Image image = getToolkit().getImage(DEF_PORTRAIT);
                        setIcon(new ImageIcon(image.getScaledInstance(-1, size, Image.SCALE_SMOOTH)));
                    }
                } 
            } else {
                if (column == COLS.UNIT.ordinal()) {
                    setToolTipText(unitTooltips.get(row));
                    final Camouflage camouflage = entity.getCamouflageOrElse(entity.getOwner().getCamouflage());
                    final Image icon = clientGui.bv.getTilesetManager().loadPreviewImage(entity, camouflage, this);
                    if (!compact) {
                        setIcon(new ImageIcon(icon.getScaledInstance(-1, size, Image.SCALE_SMOOTH)));
                        setIconTextGap(UIUtil.scaleForGUI(10));
                    } else {
                        setIcon(new ImageIcon(icon.getScaledInstance(-1, size/3, Image.SCALE_SMOOTH)));
                        setIconTextGap(UIUtil.scaleForGUI(5));
                    }
                } else if (column == COLS.PILOT.ordinal()) {
                    setToolTipText(pilotTooltips.get(row));
                    if (!compact) {
                        setIcon(new ImageIcon(entity.getCrew().getPortrait(0).getImage(size)));
                    }
                } else {
                    setToolTipText(null);
                }
            }
            
            if (column == COLS.BV.ordinal()) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }
            
            return this;
        }
        
    }
    
    @Override
    public int getColumnCount() {
        return N_COL;
    }
    
    @Override
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

} 