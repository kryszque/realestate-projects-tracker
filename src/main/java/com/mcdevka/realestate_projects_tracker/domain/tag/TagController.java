package com.mcdevka.realestate_projects_tracker.domain.tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "http://localhost:5173")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {this.tagService = tagService;}

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        try {
            List<Tag> allTags = tagService.getAllTags();
            return  ResponseEntity.ok(allTags);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        try{
            Tag createdTag = tagService.createTag(tag);
            return  ResponseEntity.ok(createdTag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        try {
            Tag tag = tagService.getTagById(id);
            return ResponseEntity.ok(tag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Tag> archiveTag(@PathVariable Long id) {
        try{
            Tag archivedTag = tagService.archiveTag(id);
            return ResponseEntity.ok(archivedTag);
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
