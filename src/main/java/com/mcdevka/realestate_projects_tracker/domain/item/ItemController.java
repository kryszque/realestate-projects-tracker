package com.mcdevka.realestate_projects_tracker.domain.item;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/projects/{projectId}/pillars/{pillarId}/items")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {this.itemService = itemService;}

    @GetMapping
    public ResponseEntity<List<Item>> getItemsForPillar(@PathVariable Long projectId, @PathVariable Long pillarId) {
        try {
            List<Item> items = itemService.getItemsForPillar(projectId, pillarId);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@PathVariable Long projectId, @PathVariable Long pillarId, @RequestBody Item item) {
        try {
            Item createdItem = itemService.createItem(projectId, pillarId, item);
            return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id) {
        try {
            Item item = itemService.getItemById(projectId, pillarId, id);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id, @RequestBody Item updatedItemData) {
        try {
            Item updatedItem = itemService.updateItem(projectId, pillarId, id, updatedItemData);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Item> archiveItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id) {
        try{
            Item archivedItem = itemService.archiveItem(projectId, pillarId, id);
            return ResponseEntity.ok(archivedItem);
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<Item> finishItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id) {
        try{
            Item finishedItem = itemService.finishItem(projectId, pillarId, id);
            return ResponseEntity.ok(finishedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Item> addTagToItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id, @PathVariable Long tagId) {
        try {
            Item updatedItem = itemService.addTagToItem(projectId, pillarId, id, tagId);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Item> removeTagFromItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id, @PathVariable Long tagId) {
        try {
            Item updatedItem = itemService.removeTagFromItem(projectId, pillarId, id, tagId);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/history")
    public ResponseEntity<ItemHistory> createItemHistory(@PathVariable Long projectId, @PathVariable Long pillarId, @PathVariable Long id, @RequestBody ItemHistory itemHistory) {
        try {
            ItemHistory createdItemHistory = itemService.addHistoryEntry(projectId, pillarId, id, itemHistory);
            return new ResponseEntity<>(createdItemHistory, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ItemHistory>> getItemHistoryByItemId(@PathVariable Long projectId, @PathVariable Long pillarId, @PathVariable Long id) {
        try {
            List<ItemHistory> history = itemService.getItemHistoryByItemId(projectId, pillarId, id);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/history/{historyId}")
    public ResponseEntity<ItemHistory> updateItemHistory(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id, @PathVariable Long historyId, @RequestBody ItemHistory updatedItemHistoryData) {
        try {
            ItemHistory updatedItemHistory = itemService.updateHistoryEntry(projectId, pillarId, id, historyId ,updatedItemHistoryData);
            return ResponseEntity.ok(updatedItemHistory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/history/{historyId}/pin")
    public ResponseEntity<ItemHistory> pinOrUnPinHistory( // <--- Bez Optional
                                                          @PathVariable Long projectId,
                                                          @PathVariable Long pillarId,
                                                          @PathVariable Long id,
                                                          @PathVariable Long historyId) {
        try {
            ItemHistory updatedItemHistory = itemService.pinOrUnPinHistory(projectId, pillarId, id, historyId);

            return ResponseEntity.ok(updatedItemHistory);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/history/{historyId}/archive")
    public ResponseEntity<ItemHistory> archiveHistory(
            @PathVariable Long projectId,
            @PathVariable Long pillarId,
            @PathVariable Long id,
            @PathVariable Long historyId) {
        try {
            ItemHistory updatedItemHistory = itemService.archiveHistory(projectId, pillarId, id, historyId);

            return ResponseEntity.ok(updatedItemHistory);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/history/{historyId}/reactions")
    public ResponseEntity<ItemHistory> toggleReaction(
            @PathVariable Long projectId,
            @PathVariable Long pillarId,
            @PathVariable Long id,
            @PathVariable Long historyId,
            @RequestBody java.util.Map<String, String> payload // Oczekujemy JSON: {"emojiCode": "❤️"}
    ) {
        String emoji = payload.get("emojiCode");
        // Tymczasowo hardcodujemy usera, dopóki nie masz Spring Security
        String currentUser = "User";

        try {
            ItemHistory updatedHistory = itemService.toggleReaction(id, historyId, emoji, currentUser);
            return ResponseEntity.ok(updatedHistory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
