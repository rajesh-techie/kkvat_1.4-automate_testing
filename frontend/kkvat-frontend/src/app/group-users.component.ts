import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-group-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './group-users.component.html',
  styleUrls: ['./group-users.component.css']
})
export class GroupUsersComponent implements OnInit {
  groupId: number | null = null;
  allUsers: any[] = [];
  selectedUserIds: Set<number> = new Set<number>();
  apiBaseGroup = 'http://localhost:8080/api/groups';
  apiBaseUsers = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.groupId = +id;
      this.loadAllUsers();
      this.loadGroupUsers();
    }
  }

  loadAllUsers() {
    this.http.get<any[]>(`${this.apiBaseUsers}/list`).subscribe(res => this.allUsers = res || []);
  }

  loadGroupUsers() {
    if (!this.groupId) return;
    this.http.get<any[]>(`${this.apiBaseGroup}/${this.groupId}/users`).subscribe(res => {
      this.selectedUserIds = new Set((res || []).map(u => u.id));
    });
  }

  toggle(userId: number) {
    if (this.selectedUserIds.has(userId)) this.selectedUserIds.delete(userId);
    else this.selectedUserIds.add(userId);
  }

  save() {
    if (!this.groupId) return;
    const body = { userIds: Array.from(this.selectedUserIds) };
    this.http.put<any>(`${this.apiBaseGroup}/${this.groupId}/users`, body).subscribe(() => {
      alert('Group membership updated');
    }, err => {
      alert('Failed to update group users');
    });
  }
}
